package com.theforceprotocol.bbd.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.theforceprotocol.bbd.consts.SmsTemplates;
import com.theforceprotocol.bbd.domain.dto.*;
import com.theforceprotocol.bbd.domain.entity.*;
import com.theforceprotocol.bbd.exception.BusinessException;
import com.theforceprotocol.bbd.props.PledgeProperties;
import com.theforceprotocol.bbd.repository.OrderLogRepository;
import com.theforceprotocol.bbd.repository.OrderRepository;
import com.theforceprotocol.bbd.repository.TokenRepository;
import com.theforceprotocol.bbd.repository.UserTxRepository;
import com.theforceprotocol.bbd.service.*;
import com.theforceprotocol.bbd.util.AmountUtils;
import com.theforceprotocol.bbd.util.Errors;
import com.theforceprotocol.bbd.util.PageUtils;
import com.theforceprotocol.bbd.util.Web3jTypeUtil;
import com.theforceprotocol.bbd.web.ContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Bytes32;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

import static com.theforceprotocol.bbd.domain.entity.OrderLog.OrderAction.*;
import static com.theforceprotocol.bbd.util.AssertUtils.isTrue;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.EnumSet.complementOf;
import static java.util.EnumSet.of;

@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private final TokenPriceService tokenPriceService;
    private final OrderRepository orderRepository;
    private final OrderLogRepository orderLogRepository;
    private final TokenRepository tokenRepository;
    private final PledgeProperties props;
    private final ContractService contractService;
    private final SmsService smsService;
    private final UserTxRepository userTxRepository;
    private final ContractTxService contractTxService;
    private final RedisLockService redisLockService;

    public OrderServiceImpl(TokenPriceService tokenPriceService,
                            OrderRepository orderRepository, OrderLogRepository orderLogRepository,
                            TokenRepository tokenRepository, PledgeProperties props,
                            ContractService contractService, SmsService smsService,
                            UserTxRepository userTxRepository, ContractTxService contractTxService
            , RedisLockService redisLockService) {
        this.tokenPriceService = tokenPriceService;
        this.orderRepository = orderRepository;
        this.orderLogRepository = orderLogRepository;
        this.tokenRepository = tokenRepository;
        this.props = props;
        this.contractService = contractService;
        this.smsService = smsService;
        this.userTxRepository = userTxRepository;
        this.contractTxService = contractTxService;
        this.redisLockService = redisLockService;
    }

    private static Collection<Order.OrderStatus> getStatuses(Status status) {
        if (status == Status.ALL) {
            return complementOf(of(Order.OrderStatus.CREATED));
        } else if (status == Status.COMPLETED) {
            return of(Order.OrderStatus.REPAID, Order.OrderStatus.FORCE_REPAID, Order.OrderStatus.CLOSED);
        } else if (status == Status.CANCELED) {
            return of(Order.OrderStatus.CANCELED);
        } else {
            return complementOf(of(Order.OrderStatus.CREATED, Order.OrderStatus.REPAID, Order.OrderStatus.FORCE_REPAID, Order.OrderStatus.CLOSED, Order.OrderStatus.CANCELED));
        }
    }

    private static void logTx(UserTx tx, Order.OrderStatus status) {
        log.info(
                "save tx, user:{},order number:{},status->{},token:{},amount:{}",
                tx.getUser(), tx.getOrder().getOrderNumber(), status,
                tx.getToken().getId(), tx.getAmount()
        );
    }

    private static boolean checkClosePosition(String data, Order order) {
        return false;
    }

    private static boolean checkForceRepay(String data, Order order) {
        return true;
    }

    private static boolean checkCallMargin(String data, Order order) {
        return false;
    }

    private static boolean checkLend(String data, Order order) {
        return false;
    }

    private static boolean checkRepay(String data, Order order) {
        return false;
    }

    private static boolean checkBorrow(String data, Order order) {
        List<Type> typeList = new ArrayList<>();
        typeList.add(new Address(order.getBorrowedToken().getId().getAccount()));
        typeList.add(new Uint(toWei(order.getReallyBorrowedAmount())));
        typeList.add(new Address(order.getPledgeToken().getId().getAccount()));
        typeList.add(new Uint(toWei(order.getPledgeAmount())));
        typeList.add(new Uint(toBigInteger(order.getNonce())));
        typeList.add(new Uint(toBigInteger(order.getDays())));
        typeList.add(new Uint(rateToBigInteger(order.getPledgeRate())));
        typeList.add(new Uint(rateToBigInteger(order.getInterestRate())));
        typeList.add(new Uint(rateToBigInteger(order.getFeeRate())));
        return compareParamsData(typeList, data);
    }

    private static boolean compareParamsData(List<Type> typeList, String respStr) {
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Function function = new Function(null, typeList, outputParameters);
        String localStr = FunctionEncoder.encode(function).substring(10);
        respStr = respStr.substring(10);
        System.out.println(localStr);
        System.out.println(respStr);
        return respStr.equals(localStr);
    }

    private void validate(String txInputDataResp, Order order) {
        boolean result = doValidate(txInputDataResp, order);
        if (!result) {
            String msg = String.format("error: validated data:%s,orderNumber:%s", txInputDataResp, order.getOrderNumber());
            throw new IllegalArgumentException(msg);
        }
    }

    private boolean doValidate(String data, Order order) {
        String methodName = data.substring(0, 10);
        if (MethodName.BORROW.methodCode.equals(methodName)) {
            return checkBorrow(data, order);
        } else if (MethodName.CANCEL_ORDER.methodCode.equals(methodName)) {
            return checkCancel(data, order);
        } else if (MethodName.REPAY.methodCode.equals(methodName)) {
            return checkRepay(data, order);
        } else if (MethodName.LEND.methodCode.equals(methodName)) {
            return checkLend(data, order);
        } else if (MethodName.CALL_MARGIN.methodCode.equals(methodName)) {
            return checkCallMargin(data, order);
        } else if (MethodName.FORCE_REPAY.methodCode.equals(methodName)) {
            return checkForceRepay(data, order);
        } else if (MethodName.CLOSE_POSITION.methodCode.equals(methodName)) {
            return checkClosePosition(data, order);
        }
        return false;
    }

    private boolean checkCancel(String data, Order order) {
        List<Type> typeList = new ArrayList<>();
        typeList.add(new Address(order.getInitiatorAccount()));
        typeList.add(new Bytes32(Web3jTypeUtil.string2Bytes32(order.getOrderHash())));
        return compareParamsData(typeList, data);
    }

    private Order newOrder(OrderCreateRequestBody body) {
        Map<TokenId, BigDecimal> map = tokenPriceService.findErcPrices();
        BigDecimal borrowedPrice = map.get(new TokenId(body.getBorrowedAccount(), body.getBorrowedSymbol()));
        BigDecimal pledgePrice = map.get(new TokenId(body.getPledgeAccount(), body.getPledgeSymbol()));
        Assert.isTrue(borrowedPrice.compareTo(ZERO) > 0 && pledgePrice.compareTo(ZERO) > 0, "invalid price");
        Assert.isTrue(validAccount(body.getAccount()), "invalid account address!");
        BigDecimal repaymentAmount = getRepaymentAmount(body);
        BigDecimal pledgeRate = (borrowedPrice.multiply(repaymentAmount))
                .divide(pledgePrice.multiply(body.getPledgeAmount()), 4, CEILING);
        BigDecimal feeRate = props.getFeeRate();
        isTrue(pledgeRate.setScale(1, FLOOR).compareTo(AmountUtils.bd("0.5")) <= 0, Errors.INVALID_PLEDGE_AMOUNT);
        Token borrowedToken = findToken(body.getBorrowedAccount(), body.getBorrowedSymbol());
        Token pledgeToken = findToken(body.getPledgeAccount(), body.getPledgeSymbol());
        Order order = new Order();
        order.setInitiator(ContextHolder.requiredCurrentUser());
        order.setInitiatorAccount(body.getAccount());
        order.setBorrowedToken(borrowedToken);
        order.setBorrowedAmount(body.getBorrowedAmount());
        order.setBorrowedPrice(borrowedPrice);
        order.setInitialBorrowedPrice(borrowedPrice);
        order.setDays(body.getDays());
        order.setPledgeToken(pledgeToken);
        order.setPledgeAmount(body.getPledgeAmount());
        order.setInitialPledgeAmount(body.getPledgeAmount());
        order.setPledgePrice(pledgePrice);
        order.setInitialPledgePrice(pledgePrice);
        order.setPledgeRate(pledgeRate);
        order.setInitialPledgeRate(pledgeRate);
        order.setInterestRate(body.getInterestRate());
        order.setFeeRate(feeRate);
        order.setRepaymentAmount(repaymentAmount);
        order.setReallyLoanedAmount(getReallyLoanedAmount(body.getBorrowedAmount(), borrowedToken.getDecimals()));
        order.setReallyBorrowedAmount(getReallyBorrowedAmount(body.getBorrowedAmount(), borrowedToken.getDecimals()));
        return order;
    }

    private boolean validAccount(String account) {
        String addressReg = "^0x[a-fA-F0-9]{40}$";
        return Pattern.matches(addressReg, account);
    }

    private BigDecimal getReallyBorrowedAmount(BigDecimal borrowedAmount, Integer decimals) {
        return (borrowedAmount.multiply(ONE.subtract(props.getFeeRate()))).setScale(decimals, FLOOR);
    }

    private BigDecimal getReallyLoanedAmount(BigDecimal borrowedAmount, Integer decimals) {
        return (borrowedAmount.multiply(ONE.add(props.getFeeRate()))).setScale(decimals, FLOOR);
    }

    private BigDecimal getRepaymentAmount(OrderCreateRequestBody body) {
        String borrowedAccount = body.getBorrowedAccount();
        String borrowedSymbol = body.getBorrowedSymbol();
        BigDecimal borrowedAmount = body.getBorrowedAmount();
        BigDecimal interestRate = body.getInterestRate();
        Integer days = body.getDays();
        return getRepaymentAmount(borrowedAccount, borrowedSymbol, borrowedAmount, interestRate, days);
    }

    private BigDecimal getRepaymentAmount(String borrowedAccount, String borrowedSymbol,
                                          BigDecimal borrowedAmount, BigDecimal interestRate,
                                          Integer days) {
        Integer decimals = findToken(borrowedAccount, borrowedSymbol).getDecimals();
        BigDecimal total = borrowedAmount.multiply(ONE.add(interestRate.multiply(AmountUtils.bd(days)).divide(AmountUtils.bd("365"), decimals + 1, CEILING)));
        return total.setScale(decimals, CEILING);
    }

    private Token findToken(String account, String symbol) {
        return tokenRepository.findByIdAndEnabledTrue(new TokenId(account, symbol))
                .orElseThrow(RuntimeException::new);
    }

    @Override
    @Transactional
    public Order createOrder(OrderCreateRequestBody body) {
        User currentUser = ContextHolder.currentUser();
        log.info("create order,user:{},body:{}", currentUser, body);
        Order order = newOrder(body);
        Order result = orderRepository.save(order);
        log.info("create order,user:{},body:{},orderNumber:{}", currentUser, body, result.getOrderNumber());
        OrderLog orderLog = new OrderLog();
        orderLog.setUser(currentUser);
        orderLog.setOrder(result);
        orderLog.setAction(CREATE);
        orderLog.setStatus(Order.OrderStatus.CREATED);
        orderLogRepository.save(orderLog);
        log.info("save order log,action:CREATE,orderNumber:{}", result.getOrderNumber());
        return result;
    }

    @Override
    public OrderCalcRespBody calc(OrderCalcRequestBody body) {
        log.info("buy in calc body:{}", body);
        Map<TokenId, BigDecimal> map = tokenPriceService.findErcPrices();
        BigDecimal borrowedPrice = map.get(new TokenId(body.getBorrowedAccount(), body.getBorrowedSymbol()));
        BigDecimal pledgePrice = map.get(new TokenId(body.getPledgeAccount(), body.getPledgeSymbol()));
        Integer pledgedTokenDecimals = findToken(body.getPledgeAccount(), body.getPledgeSymbol()).getDecimals();
        BigDecimal repaymentAmount = getRepaymentAmount(body);
        BigDecimal pledgeAmount =
                (borrowedPrice.multiply(repaymentAmount))
                        .divide(props.getPledgeRates().getMinRate(), 10, HALF_UP)
                        .divide(pledgePrice, pledgedTokenDecimals, CEILING);
        int borrowedTokenDecimals = findToken(body.getBorrowedAccount(), body.getBorrowedSymbol()).getDecimals();
        OrderCalcRespBody result = new OrderCalcRespBody();
        result.setPledgeAmount(pledgeAmount);
        result.setRepaymentAmount(repaymentAmount);
        result.setReallyLoanedAmount(getReallyLoanedAmount(body.getBorrowedAmount(), borrowedTokenDecimals));
        result.setReallyBorrowedAmount(getReallyBorrowedAmount(body.getBorrowedAmount(), borrowedTokenDecimals));
        result.setFeeRate(props.getFeeRate());
        log.info("order calc,request body:{},response body:{}", body, result);
        return result;
    }

    private BigDecimal getRepaymentAmount(OrderCalcRequestBody body) {
        String borrowedAccount = body.getBorrowedAccount();
        String borrowedSymbol = body.getBorrowedSymbol();
        BigDecimal borrowedAmount = body.getBorrowedAmount();
        BigDecimal interestRate = body.getInterestRate();
        int days = body.getDays();
        return getRepaymentAmount(borrowedAccount, borrowedSymbol,
                borrowedAmount, interestRate, days);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findOrders(Pageable pageable) {
        return orderRepository.findAllByStatusIn(of(Order.OrderStatus.PLEDGED), PageUtils.sorted(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findOrder(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void borrow(OrderBody body) throws Exception {
        redisLockService.process(body.getOrderNumber(), () -> {
            User currentUser = ContextHolder.currentUser();
            log.info("borrow: user:{},body:{}", currentUser, body);
            orderRepository.findByOrderNumber(body.getOrderNumber())
                    .ifPresent(order -> {
                        isTrue(order.getStatus() == Order.OrderStatus.CREATED, Errors.INVALID_OPERATION);
                        order.setTxId(body.getTxId());
                        order.setOrderHash(body.getOrderHash());
                        order.setNonce(body.getNonce());
                        order.setStatus(Order.OrderStatus.PLEDGE_PENDING);
                        OrderLog orderLog = new OrderLog();
                        orderLog.setUser(currentUser);
                        orderLog.setAccount(body.getAccount());
                        orderLog.setOrder(order);
                        orderLog.setAction(PLEDGE);
                        orderLog.setOrderHash(body.getOrderHash());
                        orderLog.setTxId(body.getTxId());
                        orderLog.setStatus(Order.OrderStatus.PLEDGE_PENDING);
                        orderLogRepository.save(orderLog);
                        log.info("save order log,action:PLEDGE,body:{}", body);
                    });
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void loan(ActionBody body) throws Exception {
        redisLockService.process(body.getOrderNumber(), () -> {
            User currentUser = ContextHolder.currentUser();
            log.info("loan: user:{},body:{}", currentUser, body);
            orderRepository.findByOrderNumber(body.getOrderNumber())
                    .ifPresent(order -> {
                        isTrue(order.getStatus() == Order.OrderStatus.PLEDGED, Errors.INVALID_OPERATION);
                        order.setTxId(body.getTxId());
                        order.setRecipient(ContextHolder.requiredCurrentUser());
                        order.setRecipientAccount(body.getAccount());
                        order.setStatus(Order.OrderStatus.LOAN_PENDING);
                        OrderLog orderLog = new OrderLog();
                        orderLog.setUser(currentUser);
                        orderLog.setAccount(body.getAccount());
                        orderLog.setOrder(order);
                        orderLog.setAction(LOAN);
                        orderLog.setTxId(body.getTxId());
                        orderLog.setStatus(Order.OrderStatus.LOAN_PENDING);
                        orderLogRepository.save(orderLog);
                        log.info("save order log,action:LOAN,body:{}", body);
                    });
        });
    }

    @Transactional
    @Override
    public void buyIn(ActionBody body) throws Exception {
        redisLockService.process(body.getOrderNumber(), () -> {
            User currentUser = ContextHolder.currentUser();
            log.info("buyIn: user:{},body:{}", currentUser, body);
            orderRepository.findByOrderNumber(body.getOrderNumber())
                    .ifPresent(order -> {
                        isTrue(order.getStatus() == Order.OrderStatus.LOANED, Errors.INVALID_OPERATION);
                        order.setTxId(body.getTxId());
                        order.setStatus(Order.OrderStatus.BUY_IN_PENDING);
                        OrderLog orderLog = new OrderLog();
                        orderLog.setUser(currentUser);
                        orderLog.setAccount(body.getAccount());
                        orderLog.setOrder(order);
                        orderLog.setAction(BUY_IN);
                        orderLog.setTxId(body.getTxId());
                        orderLog.setStatus(Order.OrderStatus.BUY_IN_PENDING);
                        orderLogRepository.save(orderLog);
                        log.info("save order log,action:BUY_IN,body:{}", body);
                    });
        });
    }

    @Override
    @Transactional
    public void repay(ActionBody body) throws Exception {
        redisLockService.process(body.getOrderNumber(), () -> {
            User currentUser = ContextHolder.currentUser();
            log.info("repay: user:{},body:{}", currentUser, body);
            orderRepository.findByOrderNumber(body.getOrderNumber())
                    .ifPresent(order -> {
                        isTrue(order.getStatus() == Order.OrderStatus.LOANED, Errors.INVALID_OPERATION);
                        order.setTxId(body.getTxId());
                        order.setStatus(Order.OrderStatus.REPAY_PENDING);
                        OrderLog orderLog = new OrderLog();
                        orderLog.setUser(currentUser);
                        orderLog.setAccount(body.getAccount());
                        orderLog.setOrder(order);
                        orderLog.setAction(REPAY);
                        orderLog.setTxId(body.getTxId());
                        orderLog.setStatus(Order.OrderStatus.REPAY_PENDING);
                        orderLogRepository.save(orderLog);
                        log.info("save order log,action:REPAY,body:{}", body);
                    });
        });
    }

    @Override
    @Transactional
    public void forceRepay(Order order, String memo) throws Exception {
        String orderNumber = order.getOrderNumber();
        redisLockService.process(orderNumber, () -> {
            try {
                Order dbOrder = orderRepository.findByOrderNumber(orderNumber).get();
                isTrue(dbOrder.getStatus() == Order.OrderStatus.LOANED && dbOrder.getDeadline().compareTo(Instant.now()) <= 0, Errors.INVALID_OPERATION);
                Map<TokenId, BigDecimal> map = tokenPriceService.findErcPrices();
                TokenId borrowedTokenId = dbOrder.getBorrowedToken().getId();
                TokenId pledgedTokenId = dbOrder.getPledgeToken().getId();
                BigDecimal borrowedPrice = map.get(new TokenId(borrowedTokenId.getAccount(), borrowedTokenId.getSymbol()));
                BigDecimal pledgePrice = map.get(new TokenId(pledgedTokenId.getAccount(), pledgedTokenId.getSymbol()));
                int pledgedTokenDecimals = dbOrder.getPledgeToken().getDecimals();
                BigDecimal payAmount = (dbOrder.getRepaymentAmount().multiply(borrowedPrice).multiply(AmountUtils.bd("1.25")))
                        .divide(pledgePrice, pledgedTokenDecimals, CEILING);
                isTrue(payAmount.compareTo(dbOrder.getPledgeAmount()) <= 0, Errors.INVALID_PLEDGE_AMOUNT);
                String txId = contractService.forceRepay(dbOrder.getInitiatorAccount(), dbOrder.getOrderHash(), dbOrder.getPledgeToken().getId().getAccount());
                dbOrder.setTxId(txId);
                dbOrder.setForceRepayAmount(payAmount);
                dbOrder.setStatus(Order.OrderStatus.FORCE_REPAY_PENDING);
                orderRepository.save(dbOrder);
                OrderLog orderLog = new OrderLog();
                orderLog.setUser(ContextHolder.currentUser());
                orderLog.setOrder(dbOrder);
                orderLog.setAction(FORCE_REPAY);
                orderLog.setTxId(txId);
                orderLog.setStatus(Order.OrderStatus.FORCE_REPAY_PENDING);
                orderLog.setMemo(memo);
                orderLogRepository.save(orderLog);
                log.info("force repay order:{} success,txId:{}", orderNumber, txId);
            } catch (Throwable throwable) {
                log.info("force repay order:{} failed", orderNumber, throwable);
            }
        });
    }

    @Override
    @Transactional
    public void close(Order order, String memo) throws Exception {
        String orderNumber = order.getOrderNumber();
        try {
            Order dbOrder = orderRepository.findByOrderNumber(orderNumber).get();
            log.info("close orderNumber:{},memo:{}", orderNumber, memo);
            String txId = contractService.close(dbOrder.getInitiatorAccount(), dbOrder.getOrderHash(), dbOrder.getPledgeToken().getId().getAccount());
            order.setTxId(txId);
            order.setStatus(Order.OrderStatus.CLOSE_PENDING);
            orderRepository.save(order);
            OrderLog orderLog = new OrderLog();
            orderLog.setOrder(order);
            orderLog.setAction(CLOSE);
            orderLog.setTxId(txId);
            orderLog.setStatus(Order.OrderStatus.CLOSE_PENDING);
            orderLog.setMemo(memo);
            orderLogRepository.save(orderLog);
            log.info("close order:{} success,txId:{}", orderNumber, txId);
        } catch (Throwable throwable) {
            log.info("close order:{} failed", orderNumber, throwable);
        }
    }

    @Override
    @Transactional
    public void cancel(Order order, String memo) throws Exception {
        String orderNumber = order.getOrderNumber();
        redisLockService.process(orderNumber, () -> doCancel(orderNumber, memo));
    }

    private void doCancel(String orderNumber, String memo) {
        try {
            Order dbOrder = orderRepository.findByOrderNumber(orderNumber).get();
            isTrue(of(Order.OrderStatus.PLEDGE_PENDING, Order.OrderStatus.PLEDGED, Order.OrderStatus.LOAN_PENDING).contains(dbOrder.getStatus()), Errors.INVALID_OPERATION);
            String txId = contractService.cancel(dbOrder.getInitiatorAccount(), dbOrder.getOrderHash());
            dbOrder.setTxId(txId);
            dbOrder.setStatus(Order.OrderStatus.CANCEL_PENDING);
            orderRepository.save(dbOrder);
            OrderLog orderLog = new OrderLog();
            orderLog.setOrder(dbOrder);
            orderLog.setAction(CANCEL);
            orderLog.setTxId(txId);
            orderLog.setStatus(Order.OrderStatus.CANCEL_PENDING);
            orderLog.setMemo(memo);
            orderLogRepository.save(orderLog);
            log.info("cancel order:{} success,txId:{}", orderNumber, txId);
        } catch (Throwable throwable) {
            log.info("cancel order:{} failed", orderNumber, throwable);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(OrderBody body) throws Exception {
        redisLockService.process(body.getOrderNumber(), () -> {
            User currentUser = ContextHolder.currentUser();
            log.info("cancel: user:{},body:{}", currentUser, body);
            orderRepository.findByOrderNumber(body.getOrderNumber())
                    .ifPresent(order -> {
                        isTrue(order.getStatus() == Order.OrderStatus.PLEDGED, Errors.INVALID_OPERATION);
                        order.setTxId(body.getTxId());
                        order.setStatus(Order.OrderStatus.CANCEL_PENDING);
                        OrderLog orderLog = new OrderLog();
                        orderLog.setUser(currentUser);
                        orderLog.setAccount(body.getAccount());
                        orderLog.setOrder(order);
                        orderLog.setAction(CANCEL);
                        orderLog.setTxId(body.getTxId());
                        orderLog.setStatus(Order.OrderStatus.CANCEL_PENDING);
                        orderLog.setMemo("by user");
                        orderLogRepository.save(orderLog);
                        log.info("save order log,action:CANCEL,body:{}", body);
                    });
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findPendingOrders() {
        return orderRepository.findAllByStatusIn(Order.OrderStatus.pendingStatuses());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findMyOrders(Status status, Pageable pageable) {
        User user = ContextHolder.requiredCurrentUser();
        return orderRepository.findUserOrders(user, getStatuses(status), PageUtils.sorted(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findCancelableOrders() {
        log.info("find cancelable orders");
        List<Order> result = orderRepository.findByStatusInAndCreatedDateBefore(
                of(Order.OrderStatus.PLEDGE_PENDING, Order.OrderStatus.PLEDGED, Order.OrderStatus.LOAN_PENDING),
                Instant.now().minus(7, DAYS)
        );
        log.info("find cancelable orders,result size:{}", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findForceRepayableOrders() {
        log.info("find closeable orders");
        List<Order> result = orderRepository.findByStatusInAndDeadlineBefore(of(Order.OrderStatus.LOANED), Instant.now());
        log.info("find closeable orders,result size:{}", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findNotCompletedOrders() {
        log.info("find not completed orders");
        List<Order> result = orderRepository.findByStatusIn(complementOf(of(Order.OrderStatus.CREATED, Order.OrderStatus.REPAID, Order.OrderStatus.FORCE_REPAID, Order.OrderStatus.CLOSED, Order.OrderStatus.CANCELED)));
        log.info("find not completed orders,result size:{}", result.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleValueChangingOrder(Order order) throws Exception {
        String orderNumber = order.getOrderNumber();
        redisLockService.process(orderNumber, () -> {
            try {
                Order dbOrder = orderRepository.findByOrderNumber(order.getOrderNumber()).get();
                isTrue(complementOf(of(Order.OrderStatus.CREATED, Order.OrderStatus.REPAID, Order.OrderStatus.FORCE_REPAID, Order.OrderStatus.CLOSED, Order.OrderStatus.CANCELED)).contains(dbOrder.getStatus()), Errors.INVALID_OPERATION);
                Map<TokenId, BigDecimal> map = tokenPriceService.findErcPrices();
                TokenId borrowedTokenId = order.getBorrowedToken().getId();
                TokenId pledgedTokenId = order.getPledgeToken().getId();
                BigDecimal borrowedPrice = map.get(new TokenId(borrowedTokenId.getAccount(), borrowedTokenId.getSymbol()));
                BigDecimal pledgedPrice = map.get(new TokenId(pledgedTokenId.getAccount(), pledgedTokenId.getSymbol()));
                Assert.isTrue(borrowedPrice.compareTo(ZERO) > 0 && pledgedPrice.compareTo(ZERO) > 0, "invalid price");
                BigDecimal repaymentAmount = order.getRepaymentAmount();
                BigDecimal pledgeRate = borrowedPrice.multiply(repaymentAmount)
                        .divide(pledgedPrice.multiply(order.getPledgeAmount()), 4, CEILING);
                dbOrder.setPledgeRate(pledgeRate);
                dbOrder.setBorrowedPrice(borrowedPrice);
                dbOrder.setPledgePrice(pledgedPrice);
                String memo = JSONObject.toJSONString(ImmutableMap.of(
                        "borrowedPrice", borrowedPrice,
                        "pledgedPrice", pledgedPrice,
                        "pledgeRate", pledgeRate
                ));
                if (dbOrder.getRecipient() != null && pledgeRate.compareTo(props.getPledgeRates().getMaxRate()) >= 0) {
                    close(dbOrder, memo);
                } else if (pledgeRate.compareTo(props.getPledgeRates().getAlarmRate()) >= 0) {
                    alarm(dbOrder, pledgeRate, memo);
                    boolean isIn7Days = Duration.between(dbOrder.getCreatedDate(), Instant.now()).toDays() < 7;
                    boolean isNotLoaned = dbOrder.getRecipient() == null;
                    if (isIn7Days && isNotLoaned) {
                        doCancel(orderNumber, memo);
                    }
                }
                log.info("handle value changing order:{} success", orderNumber);
            } catch (Throwable throwable) {
                log.info("handle value changing order:{} failed", orderNumber, throwable);
            }
        });
    }

    @Override
    public void alarm(Order order, BigDecimal pledgeRate, String memo) {
        User initiator = order.getInitiator();
        String orderNumber = order.getOrderNumber();
        smsService.send(
                initiator.getCountryCode(),
                initiator.getPhone(),
                String.format(SmsTemplates.PLEDGE_RATE_WARNING_TEMPLATE, orderNumber, pledgeRate)
        );
        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(order);
        orderLog.setAction(ALARM);
        orderLog.setStatus(order.getStatus());
        orderLog.setMemo(memo);
        orderLogRepository.save(orderLog);
        log.info("save alarm log,orderNumber:{},memo:{}", orderNumber, memo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePendingOrder(Order order) throws Exception {
        String orderNumber = order.getOrderNumber();
        redisLockService.process(orderNumber, () -> {
            try {
                Order dbOrder = orderRepository.findByOrderNumber(orderNumber).get();
                Order.OrderStatus status = dbOrder.getStatus();
                isTrue(Order.OrderStatus.pendingStatuses().contains(status), Errors.INVALID_OPERATION);
                String txId = dbOrder.getTxId();
                String txInputDataResp = contractTxService.findTxData(txId);
                validate(txInputDataResp, dbOrder);
                if (status == Order.OrderStatus.PLEDGE_PENDING) {
                    handlePledgePendingOrder(dbOrder);
                } else if (status == Order.OrderStatus.CANCEL_PENDING) {
                    handleCancelPendingOrder(dbOrder);
                } else if (status == Order.OrderStatus.LOAN_PENDING) {
                    handleLoanPendingOrder(dbOrder);
                } else if (status == Order.OrderStatus.BUY_IN_PENDING) {
                    handleBuyInPendingOrder(null, dbOrder);
                } else if (status == Order.OrderStatus.REPAY_PENDING) {
                    handleRepayPendingOrder(dbOrder);
                } else if (status == Order.OrderStatus.FORCE_REPAY_PENDING) {
                    handleForceRepayPendingOrder(null, dbOrder);
                } else if (status == Order.OrderStatus.CLOSE_PENDING) {
                    handleClosePendingOrder(dbOrder);
                }
                log.info("handle pending order:{} success", orderNumber);
            } catch (Throwable throwable) {
                log.info("handle pending order:{} failed", orderNumber, throwable);
            }
        });
    }

    private void handleClosePendingOrder(Order order) {
        order.setStatus(Order.OrderStatus.CLOSED);
        order.setRepaymentDate(Instant.now());
        Order savedOrder = orderRepository.save(order);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(savedOrder);
        orderLog.setAction(CONFIRM);
        orderLog.setStatus(savedOrder.getStatus());
        orderLogRepository.save(orderLog);
        log.info("save order log:{},status->{}", savedOrder.getOrderNumber(), Order.OrderStatus.CLOSED);
        UserTx tx1 = new UserTx();
        tx1.setUser(savedOrder.getInitiator());
        tx1.setOrder(savedOrder);
        tx1.setToken(savedOrder.getPledgeToken());
        tx1.setAmount(savedOrder.getPledgeAmount().negate());
        UserTx savedTx1 = userTxRepository.save(tx1);
        logTx(savedTx1, Order.OrderStatus.CLOSED);

        UserTx tx2 = new UserTx();
        tx2.setUser(savedOrder.getRecipient());
        tx2.setOrder(savedOrder);
        tx2.setToken(savedOrder.getPledgeToken());
        tx2.setAmount(savedOrder.getPledgeAmount());
        UserTx savedTx2 = userTxRepository.save(tx2);
        logTx(savedTx2, Order.OrderStatus.CLOSED);
        log.info("save order:{},status->{}", order.getOrderNumber(), Order.OrderStatus.CLOSED);
    }

    private void handleForceRepayPendingOrder(BigDecimal amount, Order order) {
        order.setStatus(Order.OrderStatus.FORCE_REPAID);
        order.setRepaymentDate(Instant.now());
        Order savedOrder = orderRepository.save(order);
        log.info("save order:{},status->{}", savedOrder.getOrderNumber(), Order.OrderStatus.FORCE_REPAID);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(savedOrder);
        orderLog.setAction(CONFIRM);
        orderLog.setStatus(savedOrder.getStatus());
        OrderLog savedLog = orderLogRepository.save(orderLog);
        log.info("save order log:{},status->{}", savedLog.getOrder().getOrderNumber(), Order.OrderStatus.FORCE_REPAID);
        UserTx tx1 = new UserTx();
        tx1.setUser(savedOrder.getInitiator());
        tx1.setOrder(savedOrder);
        tx1.setToken(savedOrder.getPledgeToken());
        tx1.setAmount(savedOrder.getPledgeAmount().subtract(amount));
        UserTx savedTx1 = userTxRepository.save(tx1);
        logTx(savedTx1, Order.OrderStatus.FORCE_REPAID);

        UserTx tx2 = new UserTx();
        tx2.setUser(savedOrder.getRecipient());
        tx2.setOrder(savedOrder);
        tx2.setToken(savedOrder.getPledgeToken());
        tx2.setAmount(amount);
        UserTx savedTx2 = userTxRepository.save(tx2);
        logTx(savedTx2, Order.OrderStatus.FORCE_REPAID);
    }

    private void handleRepayPendingOrder(Order order) {
        order.setStatus(Order.OrderStatus.REPAID);
        order.setRepaymentDate(Instant.now());
        Order savedOrder = orderRepository.save(order);
        log.info("save order:{},status->{}", savedOrder.getOrderNumber(), Order.OrderStatus.REPAID);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(savedOrder);
        orderLog.setAction(CONFIRM);
        orderLog.setStatus(savedOrder.getStatus());
        OrderLog savedLog = orderLogRepository.save(orderLog);
        log.info("save order log:{},status->{}", savedLog.getOrder().getOrderNumber(), Order.OrderStatus.REPAID);

        UserTx tx1 = new UserTx();
        tx1.setUser(savedOrder.getInitiator());
        tx1.setOrder(savedOrder);
        tx1.setToken(savedOrder.getBorrowedToken());
        tx1.setAmount(savedOrder.getRepaymentAmount().negate());

        userTxRepository.save(tx1);
        logTx(tx1, Order.OrderStatus.REPAID);

        UserTx tx2 = new UserTx();
        tx2.setUser(savedOrder.getRecipient());
        tx2.setOrder(savedOrder);
        tx2.setToken(savedOrder.getBorrowedToken());
        tx2.setAmount(savedOrder.getRepaymentAmount());
        userTxRepository.save(tx2);
        logTx(tx2, Order.OrderStatus.REPAID);
    }

    private void handleBuyInPendingOrder(BigDecimal amount, Order order) {
        order.setStatus(Order.OrderStatus.LOANED);
        order.setPledgeAmount(order.getPledgeAmount().add(amount));
        Order savedOrder = orderRepository.save(order);
        log.info("save order:{},status->{}", savedOrder.getOrderNumber(), Order.OrderStatus.LOANED);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(savedOrder);
        orderLog.setAction(CONFIRM);
        orderLog.setStatus(savedOrder.getStatus());
        orderLogRepository.save(orderLog);
        log.info("save order log:{},status->{}", savedOrder.getOrderNumber(), Order.OrderStatus.LOANED);

        UserTx tx = new UserTx();
        tx.setUser(savedOrder.getInitiator());
        tx.setOrder(savedOrder);
        tx.setToken(savedOrder.getPledgeToken());
        tx.setAmount(amount.negate());
        UserTx savedTx = userTxRepository.save(tx);
        logTx(savedTx, Order.OrderStatus.LOANED);
    }

    private void handleLoanPendingOrder(Order order) {
        String orderNumber = order.getOrderNumber();
        order.setStatus(Order.OrderStatus.LOANED);
        Instant now = Instant.now();
        order.setLoanedDate(now);
        order.setDeadline(now.plus(order.getDays(), ChronoUnit.DAYS));
        Order savedOrder = orderRepository.save(order);
        log.info("save order:{},status->{}", orderNumber, Order.OrderStatus.LOANED);

        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(savedOrder);
        orderLog.setAction(CONFIRM);
        orderLog.setStatus(savedOrder.getStatus());
        orderLogRepository.save(orderLog);
        log.info("save order log:{},status->{}", orderNumber, Order.OrderStatus.LOANED);

        UserTx tx1 = new UserTx();
        tx1.setUser(savedOrder.getInitiator());
        tx1.setOrder(savedOrder);
        tx1.setToken(savedOrder.getBorrowedToken());
        tx1.setAmount(savedOrder.getBorrowedAmount());
        userTxRepository.save(tx1);
        logTx(tx1, Order.OrderStatus.LOANED);

        UserTx tx2 = new UserTx();
        tx2.setUser(savedOrder.getRecipient());
        tx2.setOrder(savedOrder);
        tx2.setToken(savedOrder.getBorrowedToken());
        tx2.setAmount(savedOrder.getBorrowedAmount().negate());
        userTxRepository.save(tx2);
        logTx(tx2, Order.OrderStatus.LOANED);
    }

    private void handleCancelPendingOrder(Order order) {
        order.setStatus(Order.OrderStatus.CANCELED);
        Order savedOrder = orderRepository.save(order);
        log.info("save order:{},status->{}", savedOrder.getOrderNumber(), Order.OrderStatus.CANCELED);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(savedOrder);
        orderLog.setAction(CONFIRM);
        orderLog.setStatus(savedOrder.getStatus());
        OrderLog savedOrderLog = orderLogRepository.save(orderLog);
        log.info("save order log:{},status->{}", savedOrderLog.getOrder().getOrderNumber(), Order.OrderStatus.CANCELED);
        UserTx tx = new UserTx();
        tx.setUser(savedOrder.getInitiator());
        tx.setOrder(savedOrder);
        tx.setToken(savedOrder.getPledgeToken());
        tx.setAmount(savedOrder.getPledgeAmount());
        UserTx savedTx = userTxRepository.save(tx);
        logTx(savedTx, Order.OrderStatus.CANCELED);
    }

    private void handlePledgePendingOrder(Order order) {
        order.setStatus(Order.OrderStatus.PLEDGED);
        Order savedOrder = orderRepository.save(order);
        log.info("save order:{},status->{}", savedOrder.getOrderNumber(), Order.OrderStatus.PLEDGED);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(savedOrder);
        orderLog.setAction(CONFIRM);
        orderLog.setStatus(savedOrder.getStatus());
        OrderLog savedOrderLog = orderLogRepository.save(orderLog);
        log.info("save order log:{},status->{}", savedOrderLog.getOrder().getOrderNumber(), Order.OrderStatus.PLEDGED);
        UserTx tx = new UserTx();
        tx.setUser(savedOrder.getInitiator());
        tx.setOrder(savedOrder);
        tx.setToken(savedOrder.getPledgeToken());
        tx.setAmount(savedOrder.getPledgeAmount().negate());
        UserTx savedTx = userTxRepository.save(tx);
        logTx(savedTx, Order.OrderStatus.PLEDGED);
    }

    @Override
    public BuyInCalcRespBody calc(BuyInCalcRequestBody body) {
        log.info("buy in calc body:{}", body);
        Order order = findOrder(body.getOrderNumber()).orElseThrow(() -> new BusinessException(Errors.NOT_FOUND));
        Map<TokenId, BigDecimal> map = tokenPriceService.findErcPrices();
        TokenId borrowedTokenId = order.getBorrowedToken().getId();
        BigDecimal borrowedPrice = map.get(new TokenId(borrowedTokenId.getAccount(), borrowedTokenId.getSymbol()));
        TokenId pledgedTokenId = order.getPledgeToken().getId();
        BigDecimal pledgePrice = map.get(new TokenId(pledgedTokenId.getAccount(), pledgedTokenId.getSymbol()));
        Integer decimals = findToken(pledgedTokenId.getAccount(), pledgedTokenId.getSymbol()).getDecimals();
        log.info(
                "buy in: borrowedToken:{},borrowedPrice:{},pledgedToken:{}," +
                        "pledgePrice:{},current pledgeAmount:{}",
                borrowedTokenId, borrowedPrice, pledgedTokenId, pledgePrice, order.getPledgeAmount()
        );
        if (pledgePrice.compareTo(ZERO) <= 0) {
            throw new BusinessException(Errors.NO_TOKEN_PRICE_INFO);
        }
        //Assert.isTrue(pledgePrice.compareTo(ZERO) > 0, NO_TOKEN_PRICE_INFO.getMessage());
        BigDecimal amount = borrowedPrice.multiply(order.getBorrowedAmount())
                .divide(body.getTargetPledgeRate(), 10, HALF_UP)
                .divide(pledgePrice, decimals, CEILING)
                .subtract(order.getPledgeAmount())
                .setScale(decimals, CEILING);
        Assert.isTrue(amount.compareTo(ZERO) > 0, "invalid request");
        BuyInCalcRespBody result = new BuyInCalcRespBody();
        result.setAmount(amount);
        result.setClosingRate(props.getPledgeRates().getMaxRate());
        log.info("buy in calc body:{},result:{}", body, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findWarningOrders() {
        log.info("find warning orders");
        List<Order> result = orderRepository.findByStatusInAndDeadlineBetween(
                Order.OrderStatus.uncompletedStatuses(),
                Instant.now(),
                Instant.now().plus(1, DAYS)
        );
        log.info("find warning orders,result size:{}", result.size());
        return result;
    }

    @Override
    public void handleWarningOrder(Order order, String memo) throws Exception {
        String orderNumber = order.getOrderNumber();
        redisLockService.process(orderNumber, () -> {
            try {
                Order dbOrder = orderRepository.findByOrderNumber(orderNumber).get();
                isTrue(Order.OrderStatus.uncompletedStatuses().contains(dbOrder.getStatus()), Errors.INVALID_OPERATION);
                User initiator = dbOrder.getInitiator();
                Integer countryCode = initiator.getCountryCode();
                String phone = initiator.getPhone();
                smsService.send(countryCode, phone, String.format(SmsTemplates.EXPIRING_ORDER_TEMPLATE, orderNumber));
                OrderLog orderLog = new OrderLog();
                orderLog.setOrder(dbOrder);
                orderLog.setAction(ALARM);
                orderLog.setStatus(dbOrder.getStatus());
                orderLog.setMemo(memo);
                OrderLog savedOrderLog = orderLogRepository.save(orderLog);
                log.info("warn order:{} success,result:{}", orderNumber, savedOrderLog);
            } catch (Throwable throwable) {
                log.info("warn order:{} failed", orderNumber, throwable);
            }
        });
    }

    private static BigInteger toWei(BigDecimal bd) {
        return bd.movePointRight(18).toBigIntegerExact();
    }

    private static BigInteger toBigInteger(Integer num) {
        return new BigInteger(num.toString());
    }

    private static BigInteger rateToBigInteger(BigDecimal bd) {
        return bd.movePointRight(4).toBigIntegerExact();
    }

}
