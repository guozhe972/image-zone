package com.cncsys.imgz.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.cncsys.imgz.entity.OrderEntity;
import com.cncsys.imgz.helper.CodeParser;
import com.cncsys.imgz.helper.MailHelper;
import com.cncsys.imgz.service.AccountService;
import com.cncsys.imgz.service.AsyncService;
import com.cncsys.imgz.service.OrderService;

@Controller
public class PayController {
	private static final Logger logger = LoggerFactory.getLogger(PayController.class);

	@Value("${alipay.public.key}")
	private String ALIPAY_PUBLIC;

	@Value("${alipay.cost.rate}")
	private String ALIPAY_RATE;

	@Value("${alipay.app.id}")
	private String ALIPAY_APPID;

	@Value("${alipay.seller.id}")
	private String ALIPAY_SELLER;

	@Value("${order.download.days}")
	private int DOWNLOAD_DAYS;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CodeParser codeParser;

	@Autowired
	private MailHelper mailHelper;

	@Autowired
	private AsyncService asyncService;

	@Autowired
	private AccountService accountService;

	@PostMapping("/alipay/notify")
	public void aliNotify(HttpServletRequest request, HttpServletResponse response,
			UriComponentsBuilder builder, Locale locale) throws AlipayApiException, IOException {
		String trade_no = request.getParameter("trade_no");
		logger.info("trade_no:" + trade_no);

		Map<String, String> params = new HashMap<String, String>();
		Map<String, String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}

		String orderno = params.get("out_trade_no");
		logger.info("order_no:" + orderno);
		String token = params.get("passback_params");
		String downlink = "/download/" + orderno + "/" + token;
		String email = codeParser.decrypt(token);
		logger.info("email_addr:" + email);

		String result = "failure";
		boolean signVerified = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC, "utf-8", "RSA2");
		if (signVerified) {
			logger.info("alipay notify OK");
			String appId = params.get("app_id");
			String sellerId = params.get("seller_id");
			if (ALIPAY_APPID.equals(appId) && ALIPAY_SELLER.equals(sellerId)) {
				String tradeStatus = params.get("trade_status");
				logger.info("trade_status:" + tradeStatus);
				if (tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")) {
					int amount = 0;
					List<OrderEntity> order = orderService.selectOrder(orderno, email, false);
					for (OrderEntity entity : order) {
						amount += entity.getPrice();
					}

					String totalAmount = params.get("total_amount");
					logger.info("total_amount:" + totalAmount);
					int total = Integer.parseInt(totalAmount.replace(".00", ""));
					if (order.size() > 0 && total == amount) {
						LocalDate expiredt = LocalDate.now().plusDays(DOWNLOAD_DAYS);
						orderService.updateOrder(orderno, email, expiredt);

						// send order mail
						if (!email.equals(orderno)) {
							String[] param = new String[3];
							param[0] = orderno;
							param[1] = expiredt.toString();
							param[2] = builder.path(downlink).build().toUriString();
							mailHelper.sendOrderDone(email, param);
						}

						// update balance
						String username = order.get(0).getUsername();
						BigDecimal fee = BigDecimal.valueOf(amount).multiply(new BigDecimal(ALIPAY_RATE)).divide(
								BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
						accountService.updateBalance(username, amount, BigDecimal.valueOf(amount).subtract(fee));
					}
					result = "success";
				}
			}
		} else {
			logger.warn("alipay notify NG");
			asyncService.deleteOrder(orderno);
		}
		logger.info("result:" + result);
		response.getWriter().println(result);
	}
}
