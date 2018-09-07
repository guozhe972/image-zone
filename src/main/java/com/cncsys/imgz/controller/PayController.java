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

		String orderno = request.getParameter("out_trade_no");
		String token = request.getParameter("passback_params");
		String downlink = "/download/" + orderno + "/" + token;
		String email = codeParser.decrypt(token);

		String result = "failure";
		boolean signVerified = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC, "utf-8", "RSA2");
		if (signVerified) {
			logger.info("alipay notify ok.");
			//String appId = request.getParameter("app_id");
			//String tradeNo = request.getParameter("trade_no");
			//String receiptAmount = request.getParameter("receipt_amount");
			//String sellerId = request.getParameter("seller_id");
			String tradeStatus = request.getParameter("trade_status");
			logger.info("trade_status:" + tradeStatus);
			if (tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")) {
				int amount = 0;
				List<OrderEntity> order = orderService.selectOrder(orderno, email);
				for (OrderEntity entity : order) {
					amount += entity.getPrice();
				}

				String totalAmount = request.getParameter("total_amount");
				int total = Integer.parseInt(totalAmount.replace(".00", ""));
				if (total != 0 && total == amount) {
					LocalDate expiredt = LocalDate.now().plusDays(DOWNLOAD_DAYS);
					int updCount = orderService.updateOrder(orderno, email, expiredt);
					if (updCount > 0) {
						if (!email.equals(orderno)) {
							// send order mail
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
				}
				result = "success";
			}
		} else {
			logger.info("alipay notify ng.");
			asyncService.deleteOrder(orderno);
		}

		response.getWriter().println(result);
	}
}
