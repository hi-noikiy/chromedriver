package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.mihuashi.action.LoadMoreContentAction;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.ClickAction;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理需求页面 解析 project
 * 示例URL:
 */
public class ProjectTask extends Task {

	public Project project;

	public String moreRatingPath = "#vue-comments-app > div:nth-child(2) > a";

	public String moreProjectPath = "#artworks > div > section > div:nth-child(2) > a";

	public String ratingPath = "#users-show > div.container-fluid > div.profile__container > main > header > ul > li:nth-child(2) > a";

	public ProjectTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);
		this.setBuildDom();

		// 设置优先级
		this.setPriority(Priority.HIGH);

		this.addDoneCallback(() -> {
			Document doc = getResponse().getDoc();
			String src = getResponse().getText();

			//页面错误
			if ( src.contains("非常抱歉") || src.contains("权限不足") ) {
				try {
					ChromeDriverRequester.getInstance().submit(new ProjectTask(getUrl()));
					return;
				} catch (MalformedURLException | URISyntaxException e) {
					logger.error("添加任务失败", e);
				}
			}

			//下载页面
			FileUtil.writeBytesToFile(src.getBytes(), "project.html");

			//抓取页面
			crawlJob(doc);
		});
	}

	public void crawlJob(Document doc){

		project = new Project(getUrl());

		List<Task> tasks = new ArrayList();
		project.domain_id = 4;
		String authorUrl = null;

		//项目名
		String title = doc.select("#project-name").text();
		String renzheng = doc.select("#project-name > span").text();
		project.title = title.replace(renzheng,"");

		//抓取发布时间
		String time = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__info-section > section > section > div.pull-left > p")
				.text().replace("企划发布于","").replace("\"","");
		String pub = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__info-section > section > section > div.pull-left > p > span").text();
		String pubTime = time.replace(pub,"");
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");

		//设置发布时间
		try {
			project.pubdate = format1.parse(pubTime);
		} catch (ParseException e) {
			logger.error("error on String to data");
		}

		//类型
		project.category = pub;

		//预算
		String budget = doc.select("#aside-rail > div > aside > p:nth-child(4)").text().replace("￥","");
		if(budget!=null&&!"".equals(budget)){
			if(budget.contains("~")){
				String[] budgets = budget.split("~");
				String budget_lb = CrawlerAction.getNumbers(budgets[0]);
				String budget_uo = CrawlerAction.getNumbers(budgets[1]);
				project.budget_lb = Integer.valueOf(budget_lb);
				project.budget_ub = Integer.valueOf(budget_uo);
			}else{
				project.budget_ub=project.budget_lb=Integer.valueOf(budget);
			}
		}

		//截止时间
		String remainingTime = doc.select("#aside-rail > div > aside > p:nth-child(2)").text();
		try {
			project.due_time = DateFormatUtil.parseTime(remainingTime);
		} catch (ParseException e) {
			logger.error("error on String to date",e);
		}

		//工期,截止时间 - 发布时间
		String endTime = doc.select("#aside-rail > div > aside > p:nth-child(2)").text();
		try {
			long eTime = DateFormatUtil.parseTime(endTime).getTime();
			long timeLimt = eTime - project.pubdate.getTime();
			project.time_limit = Integer.valueOf((int)(timeLimt / (1000 * 60 * 60 *24)));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		//描述
		project.content = doc.select("#project-detail").html();

		//资金分配
		project.delivery_steps = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__info-section > section > div.deposit-phases__wrapper").text();

		//采集时刻以投标数
		String bidderNewNum = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__application-section > section > h5 > div > span.applications-count")
				.text().replace("本企划共应征画师","").replace("名","");
		String num = CrawlerAction.getNumbers(bidderNewNum);
		if(num!=null&&!"".equals(num)){
			project.bids_num= Integer.valueOf(num);
		}

		//投标人姓名
		project.tenderer_name = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__sidebar-container > aside > section > h5 > span").text();

		//投标人ID
		String tendererId = doc.select("#profile__avatar > a").attr("href").toString();
		Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=employer");
		Matcher matcher = pattern.matcher(tendererId);

		//抓取 tenderer_id , 并转换字符集
		String tenderer_id = null;
		while(matcher.find()) {
			try {
				tenderer_id = URLDecoder.decode(matcher.group("username"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		//抓取 tenderer_name
		if( project.tenderer_name == null || "".equals(project.tenderer_name) ){
			project.tenderer_name = tenderer_id;
		}

		//采集招标人信息
		if(tenderer_id != null
				&& ! "".equals(tenderer_id)
				)
		{
			//招标人详情页url
			authorUrl = "https://www.mihuashi.com/users/"+tenderer_id+"?role=employer";

			project.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
					one.rewind.txt.StringUtil.uuid(authorUrl));

			try {

				//添加甲方任务
				Task taskT = new TendererTask(authorUrl);
				taskT.addAction(new LoadMoreContentAction(moreProjectPath));

				tasks.add(taskT);

				//添加甲方评论任务
				Task taskTR = new TendererRatingTask(authorUrl+"&rating=true");

				taskTR.addAction(new ClickAction(ratingPath));
				taskTR.addAction(new LoadMoreContentAction(moreRatingPath));

				tasks.add(taskTR);

			} catch (MalformedURLException | URISyntaxException e) {
				logger.error("Error extract url: {}, ", authorUrl, e);
			}
		}

		for(Task t : tasks){
			ChromeDriverRequester.getInstance().submit(t);
		}

		try {
			project.insert();
		} catch (Exception e) {
			logger.error("error on insert project", e);
		}
	}

	@Override
	public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
		return null;
	}

}
