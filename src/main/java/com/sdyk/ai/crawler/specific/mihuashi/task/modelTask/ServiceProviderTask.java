package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.ServiceProvider;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceProviderTask extends com.sdyk.ai.crawler.task.Task {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("service_id", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("q", "ip");
		// url_template
		url_template = "https://www.mihuashi.com/users/{{service_id}}?role=painter";
	}

	ServiceProvider serviceProvider;

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();
			String src = getResponse().getText();

			if( src.contains("迷路啦") || src.contains("非常抱歉") ) {
				return;
			}
			//页面正常
			else{
				try {
					crawlerJob(doc);
				} catch (ChromeDriverException.IllegalStatusException e) {
					logger.info("error on crawlerJob",e);
				}
			}

		});
	}

	public void crawlerJob(Document doc) throws ChromeDriverException.IllegalStatusException {

		serviceProvider = new ServiceProvider(getUrl());
		List<Task> tasks = new ArrayList<Task>();
		String[] url = getUrl().split("users");

		try {
			serviceProvider.origin_id = URLDecoder.decode(url[1], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("error for 字符集转换", e);
		}

		//名字
		String name = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > h5").text();
		String renzhang = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > h5 > span").text();
		serviceProvider.name = name.replace(renzhang,"");

		//介绍
		String content = doc.select("section.profile__summary-wrapper").html();
		if( content != null && !"".equals(content) ) {
			content = doc.select("section.summary").html();
		}
		serviceProvider.content = content;

		//评论数
		String ratNum = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > section.credit > p")
				.text().replace("共","").replace("条评价","");
		ratNum = CrawlerAction.getNumbers(ratNum);
		if(ratNum!=null&&!"".equals(ratNum)){
			serviceProvider.rating_num = Integer.valueOf(ratNum);
		}

		//领域
		serviceProvider.tags = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__skill-wrapper").text();

		//项目数
		String projectNum = doc.select("#users-show > div.container-fluid > div.profile__container > main > header > ul > li.active > a > span").text();
		if( projectNum != null && !"".equals(projectNum) ){
			serviceProvider.project_num = Integer.valueOf(projectNum);
		}

		//服务质量
		String serviceQuality = doc.select("#credit > div:nth-child(1) > section > section > div:nth-child(1) > span.percent.hidden-md.visible-lg-inline-block").text().replace("%","");
		if( serviceQuality!=null && !"".equals(serviceQuality) ){
			serviceProvider.service_quality = Double.valueOf(serviceQuality) / 20;
		}

		//服务速度
		String serviceSpeed = doc.select("#credit > div:nth-child(1) > section > section > div:nth-child(2) > span.percent.hidden-md.visible-lg-inline-block").text().replace("%","");
		if( serviceSpeed!=null && !"".equals(serviceSpeed) ){
			serviceProvider.service_speed = Double.valueOf(serviceSpeed) /20;
		}

		//服务态度
		String serviceAttitude = doc.select("#credit > div:nth-child(1) > section > section > div:nth-child(3) > span.percent.hidden-md.visible-lg-inline-block").text().replace("%","");
		if( serviceAttitude!=null && !"".equals(serviceAttitude)){
			serviceProvider.service_attitude = Double.valueOf(serviceAttitude) / 20;
		}

		//头像
		Set<String> fileUrl =new HashSet<>();
		List<String> fileName = new ArrayList<>();
		String image = doc.select("#profile__avatar > img").toString();
		String imageUrl = doc.select("img.profile__avatar-image").attr("src");
		fileUrl.add(imageUrl);

		serviceProvider.head_portrait = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(imageUrl));

		BinaryDownloader.download(image,fileUrl,getUrl(),fileName);

		//作品图像
		String allImags = doc.getElementsByClass("masonry").toString();
		Pattern pattern = Pattern.compile("https://images.mihuashi.com/(?<imageSrc>.+?)\">");
		Matcher matcher = pattern.matcher(allImags);
		Set<String> imageSrcSet = new HashSet<>();

		//设置图片链接
		while(matcher.find()) {
			imageSrcSet.add("https://images.mihuashi.com/" + matcher.group("imageSrc"));
		}

		//下载图片
		BinaryDownloader.download(allImags,imageSrcSet,getUrl(),fileName);

		StringBuffer cover_images = new StringBuffer();

		for(String im : imageSrcSet){
			cover_images.append(one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(im)));
			cover_images.append(",");
		}

		serviceProvider.cover_images = cover_images.substring(0,cover_images.length()-1);

		serviceProvider.insert();

	}
}