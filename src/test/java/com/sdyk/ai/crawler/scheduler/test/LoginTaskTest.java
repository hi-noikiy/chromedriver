package com.sdyk.ai.crawler.scheduler.test;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static one.rewind.util.FileUtil.readFileByLines;

public class LoginTaskTest {

	@Test
	public void testLoginTask() throws Exception {

		ChromeDriverDistributor.instance = new Distributor();

		DockerHostManager.getInstance().delAllDockerContainers();

		AccountManager.getInstance().setAllAccountFree();

		ProxyManager.getInstance().setAllProxyFree();

		DockerHostManager.getInstance().createDockerContainers(1);

		ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

		final URL remoteAddress = container_new.getRemoteAddress();

		ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

		ChromeDriverAgent agent = new ChromeDriverAgent(remoteAddress, container_new, proxy_new);

		((Distributor)ChromeDriverDistributor.getInstance()).addAgent(agent);

		// 设置普通任务
		/*Map<String, Object> init_map_ = new HashMap<>();

		init_map_.put("youyaoqidm", "youyaoqidm");

		Class clazz_ = Class.forName("com.sdyk.ai.crawler.scheduler.test.CommonTask");

		ChromeTaskHolder holder_ = ChromeTask.buildHolder(clazz_, init_map_ );

		((Distributor)ChromeDriverDistributor.getInstance()).submit(holder_);*/

		Thread.sleep(120000);

		// 登陆任务
		LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/clouderwork.com.json"));

		Account account = AccountManager.getInstance().getAccountByDomain("clouderwork.com");

		((LoginAction)loginTask.getActions().get(loginTask.getActions().size() - 1)).setAccount(account);

		((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);

		Thread.sleep(10000000);

	}

	@Test
	public void testAllLoginTask() throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();

		AccountManager.getInstance().setAllAccountFree();

		List<Domain> domainList = Domain.getAll();
		Map<String, LoginTask> loginTasks = new HashedMap();

		File file = new File("login_tasks");
		File[] tempList = file.listFiles();

		for( File f : tempList ){
			loginTasks.put(
					f.getName().replace(".json",""),
					LoginTask.buildFromJson(readFileByLines("login_tasks/" + f.getName())) );
		}

		for( Domain d : domainList ){

			Account account = AccountManager.getInstance().getAccountByDomain(d.domain);

			LoginTask loginTask = loginTasks.get(d.domain);

			((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

			agent.submit(loginTask);

		}

		Thread.sleep(10000000);

	}

	@Test
	public void testMihuashiLoginTask() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();

		AccountManager.getInstance().setAllAccountFree();

		String domain = "mihuashi.com";

		LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/mihuashi.com.json"));

		Account account = AccountManager.getInstance().getAccountByDomain(domain);

		((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

		agent.submit(loginTask);

		Thread.sleep(100000000 );
	}


}
