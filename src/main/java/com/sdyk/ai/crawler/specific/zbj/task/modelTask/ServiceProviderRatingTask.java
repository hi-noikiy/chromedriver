package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.sdyk.ai.crawler.model.ServiceProviderRating;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import org.openqa.selenium.NoSuchElementException;
import one.rewind.txt.DateFormatUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ServiceProviderRatingTask extends ScanTask {

	ServiceProviderRating serviceProviderRating;

	// http://shop.zbj.com/evaluation/evallist-uid-7791034-type-1-isLazyload-0-page-1.html
	public static ServiceProviderRatingTask generateTask(String userId, int page) {

		String url_ = "http://shop.zbj.com/evaluation/evallist-uid-" + userId + "-category-1-isLazyload-0-page-" + page + ".html";

		try {
			ServiceProviderRatingTask t = new ServiceProviderRatingTask(url_, userId, page);
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	public ServiceProviderRatingTask(String url, String userId, int page) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("page", page);
		this.setParam("userId", userId);

		this.addDoneCallback(() -> {

			try {

				Document doc = getResponse().getDoc();

				List<com.sdyk.ai.crawler.task.Task> tasks = new ArrayList<>();

				// 判断当前页面有多少评论
				int size = 0;

				try {
					size = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl.user-information.clearfix")
							.size();
				} catch (NoSuchElementException e) {
					// 页面为空，size = 0 ，不采集数据
				}

				for (int i = 1; i <= size; i++) {

					// 防止每个评论的url一样导致id相同
					serviceProviderRating = new ServiceProviderRating(getUrl() + "--number:" + i);

					// 每个评价
					ratingData(doc, i, userId);

					try {
						serviceProviderRating.insert();
					} catch (Exception e) {
						logger.error("Error insert: {}, ", e);
					}
				}

				// 翻页 #userlist > div.pagination > ul > li.disabled
				if (pageTurning("#userlist > div.pagination > ul > li", page)) {
					com.sdyk.ai.crawler.task.Task task = generateTask(userId, page + 1);
					tasks.add(task);
				}

				for (com.sdyk.ai.crawler.task.Task t : tasks) {
					ChromeDriverRequester.getInstance().submit(t);
				}
			} catch (Exception e) {
				logger.error(e);
			}
		});
	}

	/**
	 * 获取数据
	 * @param i
	 */
	public void ratingData(Document doc, int i, String userId) {

		serviceProviderRating.service_provider_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid("https://shop.zbj.com/"+ userId +"/"));

		String[] ss = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dt > img")
				.attr("src").split("/");

		String url = "https://home.zbj.com/" + ss[3].substring(1)+ss[4]+ss[5]+ss[6].split("_")[2].split(".jpg")[0];

		serviceProviderRating.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(url));

		// http://task.zbj.com/13420593/
		String proUrl = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.name-tit > a")
				.attr("href");
		serviceProviderRating.project_id = one.rewind.txt.StringUtil.byteArrayToHex(
				one.rewind.txt.StringUtil.uuid(
						proUrl.substring(0, proUrl.length()-2)));

		serviceProviderRating.tenderer_name = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.name-tit")
				.text().split("成交价格：")[0];

		serviceProviderRating.price = Double.parseDouble(doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.name-tit")
				.text().split("成交价格：")[1].replaceAll("元", ""));

		serviceProviderRating.content = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p:nth-child(2) > span")
				.text();

		serviceProviderRating.tags = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.yingx")
					.text().split("印象：")[1];

		try {
			serviceProviderRating.pubdate = DateFormatUtil.parseTime(doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd.mint > p").text());
		} catch (ParseException e) {
			logger.error("serviceProviderRating  pubdate {}", e);
		}

	}

	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), this.getParamString("userId"), this.getParamString("page"));
	}

	@Override
	public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
		return null;
	}
}