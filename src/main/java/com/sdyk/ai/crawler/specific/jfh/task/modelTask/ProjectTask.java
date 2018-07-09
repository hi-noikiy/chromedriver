package com.sdyk.ai.crawler.specific.jfh.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.j256.ormlite.field.DatabaseField;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.jfh.task.Task;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class ProjectTask extends Task {

	static {
		registerBuilder(
				ProjectTask.class,
				"https://www.jfh.com/jfportal/orders/jf{{project_id}}",
				ImmutableMap.of("project_id", String.class),
				ImmutableMap.of("project_id", "")
		);
	}

	public Project project;

	public ProjectTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawler(doc);

		});
	}

	public void crawler(Document doc){

		project = new Project(getUrl());

		project.origin_id = getUrl().split("orders/jf")[1];

		project.domain_id = Integer.valueOf(6);

		//标题
		project.title = doc.select("#wrap > div.orderexhead > div > div.orderextop_det > h3")
				.text().replace("关注","");

		//发布时间与截止时间
		try {
			project.pubdate = DateFormatUtil.parseTime(
					doc.select("#fabuTime").text());
			project.due_time = DateFormatUtil.parseTime(
					doc.select("#wrap > div.orderexhead > div > div.orderextop_det > p > span:nth-child(3)").text());
		} catch (ParseException e) {
			logger.error("error for String to Date", e);
		}


		//价格
		String price = doc.select("#wrap > div.orderexhead > div > div.ordercstate_top_price > span.font18.b")
				.text().replace(",","");
		price = CrawlerAction.getNumbers(price);
		if( !"".equals(price) ){
			project.budget_ub = Double.valueOf(price);
			project.budget_lb = Double.valueOf(price);
		}

		Elements elements = doc.select("div.lds_list > ul > li");
		String tags = "";
		for(Element element : elements){
			String detail = element.text();

			//行业大类
			if( detail.contains("所属类目") ){
				project.category = detail.replace("所属类目：","");
			}
			//领业
			else if( detail.contains("领域") ){
				tags = tags + detail.replace("行业/领域：","");
			}
			//技能
			else if( detail.contains("技能") ) {
				tags = tags + detail.replace("技能要求：","");
			}
		}
		project.tags = tags;

		//描述
		project.content = doc.select("div.lds_des_main").toString();

		//以投标数
		String bisdNum = doc.select("#people_change").text();
		bisdNum = CrawlerAction.getNumbers(bisdNum);
		if( !"".equals(bisdNum) ){
			project.bids_num = Integer.valueOf(bisdNum);
		}

		try{
			project.insert();
		} catch (Exception e) {
			logger.error("error for project.insert", e);
		}

	}
}
