package com.sdyk.ai.crawler.requester.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.specific.mihuashi.action.LoadMoreContentAction;
import net.lightbody.bmp.BrowserMobProxyServer;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.docker.model.DockerHost;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.chrome.action.ClickAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.chrome.action.PostAction;
import one.rewind.io.requester.chrome.action.RedirectAction;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.proxy.ProxyImpl;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.json.JSON;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;

import static one.rewind.io.requester.chrome.ChromeDriverDistributor.buildBMProxy;

public class ChromeDriverDistributorTest {

	@Before
	public void loadClass() throws Exception {

		Class.forName(one.rewind.io.requester.test.TestChromeTask.class.getName());
		Class.forName(one.rewind.io.requester.test.TestChromeTask.T1.class.getName());
		Class.forName(one.rewind.io.requester.test.TestChromeTask.T2.class.getName());
		Class.forName(one.rewind.io.requester.test.TestChromeTask.T3.class.getName());
		Class.forName(one.rewind.io.requester.test.TestChromeTask.T4.class.getName());
		Class.forName(TestFailedChromeTask.class.getName());
	}

	/**
	 * 4浏览器 并发请求100个任务
	 * @throws Exception
	 */
	@Test
	public void basicTest() throws Exception {

		ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

		for(int i=0; i<4; i++) {

			ChromeDriverAgent agent = new ChromeDriverAgent();
			distributor.addAgent(agent);
		}

		distributor.layout();

		/*for(ChromeDriverAgent agent : distributor.queues.keySet()) {
			System.err.println(JSON.toPrettyJson(agent.getInfo()));
		}*/

		for(int i=0; i<1000; i++) {

			/*if(i%2 == 0) {
				ChromeTaskHolder holder = ChromeTask.buildHolder(
						one.rewind.io.requester.test.TestChromeTask.T1.class, ImmutableMap.of("q", String.valueOf(1950 + i)));

				Map<String, Object> info = distributor.submit(holder);
			} else {
				ChromeTaskHolder holder = ChromeTask.buildHolder(
						one.rewind.io.requester.test.TestChromeTask.T2.class, ImmutableMap.of("k", String.valueOf(1950 + i)));

				Map<String, Object> info = distributor.submit(holder);
			}*/
		}

		Thread.sleep(60000);

		distributor.close();
	}

	@Test
	public void recursiveTest() throws Exception {

		ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

		for(int i=0; i<4; i++) {

			ChromeDriverAgent agent = new ChromeDriverAgent();
			distributor.addAgent(agent);
		}

		distributor.layout();

		for(int i=0; i<10; i++) {

			/*ChromeTaskHolder holder = ChromeTask.buildHolder(
					one.rewind.io.requester.test.TestChromeTask.T3.class,
					ImmutableMap.of("k", String.valueOf(1950 + i)));

			Map<String, Object> info = distributor.submit(holder);*/
		}

		Thread.sleep(60000);

		distributor.close();
	}

	/**
	 * 4浏览器 并发请求100个任务
	 * @throws Exception
	 */
	@Test
	public void scheduleTaskTest() throws Exception {

		ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

		for(int i=0; i<4; i++) {

			ChromeDriverAgent agent = new ChromeDriverAgent();
			distributor.addAgent(agent);
		}

		distributor.layout();

		/*ChromeTaskHolder holder = ChromeTask.buildHolder(
				one.rewind.io.requester.test.TestChromeTask.T3.class, ImmutableMap.of("k", String.valueOf(1950)));

		Map<String, Object> info = ChromeTaskScheduler.getInstance().schedule(new ScheduledChromeTask(holder, "* * * * *"));*/

		//System.err.println(JSON.toPrettyJson(info));

		Thread.sleep(600000);

		distributor.close();
	}

	@Test
	public void proxyTest() throws Exception {

		ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

		Proxy proxy = new ProxyImpl("scisaga.net", 60103, "tfelab", "TfeLAB2@15");
		ChromeDriverAgent agent1 = new ChromeDriverAgent(proxy);
		distributor.addAgent(agent1);

		proxy = new ProxyImpl("114.215.70.14", 59998, "tfelab", "TfeLAB2@15");
		ChromeDriverAgent agent2 = new ChromeDriverAgent(proxy);
		distributor.addAgent(agent2);
		proxy = new ProxyImpl("118.190.133.34", 59998, "tfelab", "TfeLAB2@15");
		ChromeDriverAgent agent3 = new ChromeDriverAgent(proxy);
		distributor.addAgent(agent3);

		proxy = new ProxyImpl("118.190.44.184", 59998, "tfelab", "TfeLAB2@15");
		ChromeDriverAgent agent4 = new ChromeDriverAgent(proxy);
		distributor.addAgent(agent4);

		distributor.layout();

		Account account = new AccountImpl("zbj.com", "15284812411", "123456");

		for(int i=0; i<1; i++) {

			/*ChromeTaskHolder holder =
					ChromeTask.buildHolder(one.rewind.io.requester.test.TestChromeTask.T1.class, ImmutableMap.of("q", "ip"));

			distributor.submit(holder);*/
		}

		/*for(ChromeDriverAgent agent : distributor.queues.keySet()) {
			System.err.println(JSON.toPrettyJson(agent.getInfo()));
		}*/

		Thread.sleep(60000);

		distributor.close();
	}

	/**
	 * 手动抛出异常
	 * 查看ChromeDriverAgent重启情况
	 * @throws Exception
	 */
	@Test
	public void ExceptionTest() throws Exception {

		ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

		for(int i=0; i<1; i++) {

			ChromeDriverAgent agent = new ChromeDriverAgent();
			distributor.addAgent(agent);
		}

		distributor.layout();

		for(int i=0; i<10; i++) {

			/*ChromeTaskHolder holder = ChromeTask.buildHolder(
					TestFailedChromeTask.class, ImmutableMap.of("q", "ip"));

			distributor.submit(holder);*/
		}

		Thread.sleep(6000000);

		distributor.close();
	}

	@Test
	public void test() throws Exception {

		Class.forName(TestFailedChromeTask.class.getName());

		/*ChromeTaskHolder holder = ChromeTask.buildHolder(
				TestFailedChromeTask.class, ImmutableMap.of("q", "ip"));*/

	}

	/**
	 * 账户异常回调
	 * @throws ChromeDriverException.IllegalStatusException
	 * @throws InterruptedException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws ChromeDriverException.NotFoundException
	 * @throws AccountException.NotFound
	 */
	@Test
	public void testAccountFailed() throws Exception {

		Class.forName(TestFailedChromeTask.class.getName());

		int containerNum = 1;

		DockerHost host = new DockerHost("10.0.0.50", 22, "root");
		host.delAllDockerContainers();

		ChromeDriverDockerContainer container = host.createChromeDriverDockerContainer();

		ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

		ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress(), container);

		distributor.addAgent(agent);
		//ChromeDriverAgent agent = new ChromeDriverAgent(remoteAddress);

		AccountImpl account_1 = new AccountImpl("zbj.com", "17600668061", "gcy116149");
		AccountImpl account_2 = new AccountImpl("zbj.com", "15284809626", "123456");

		ChromeTask task = new ChromeTask("http://www.zbj.com")
				.addAction(new LoginWithGeetestAction());

		//
		agent.submit(task, true);

		agent.addAccountFailedCallback((a, acc) -> {

			try {
				ChromeTask task1 = new ChromeTask("http://www.zbj.com")
						.addAction(new RedirectAction("https://login.zbj.com/login/dologout"))
						.addAction(new LoginWithGeetestAction());


				a.submit(task1, true);

			} catch (Exception e) {
				e.printStackTrace();
			}

		});

		/*ChromeTaskHolder holder = ChromeTask.buildHolder(
				TestFailedChromeTask.class, ImmutableMap.of("q", "ip"));

		distributor.submit(holder);*/

		Thread.sleep(6000000);

		distributor.close();

	}

	/**
	 * 代理异常回调
	 * @throws Exception
	 */
	@Test
	public void testProxyFailed() throws Exception {

		ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

		Proxy proxy1 = new ProxyImpl("114.215.70.14", 59998, "tfelab", "TfeLAB2@15");

		Proxy proxy2 = new ProxyImpl("118.190.133.34", 59998, "tfelab", "TfeLAB2@15");

		ChromeDriverAgent agent = new ChromeDriverAgent(proxy1);

		agent.addProxyFailedCallback((a, p, t) -> {

			try {
				a.changeProxy(proxy2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ChromeDriverException.IllegalStatusException e) {
				e.printStackTrace();
			}
		});

		/*ChromeTask task = new ChromeTask("https://www.baidu.com/s?wd=ip");

		task.setValidator((a, t) -> {

			logger.info("proxy");
			//throw new UnreachableBrowserException("Test");
			throw new ProxyException.Failed(a.proxy);
			//throw new AccountException.Failed(account);
		});

		ChromeTask task1 = new ChromeTask("https://www.baidu.com/s?wd=ip");

		//
		agent.addNewCallback((a)->{
			try {
				a.submit(task1);
			} catch (ChromeDriverException.IllegalStatusException e) {
				e.printStackTrace();
			}
		});*/

		distributor.addAgent(agent);

		/*ChromeTaskHolder holder = ChromeTask.buildHolder(
				TestFailedChromeTask.class, ImmutableMap.of("q", "ip"));

		distributor.submit(holder);*/

		Thread.sleep(6000000);

		distributor.close();
	}

	@Test
	public void testBuildProxyServer() throws InterruptedException, UnknownHostException {

		Proxy proxy = new ProxyImpl("scisaga.net", 60103, "tfelab", "TfeLAB2@15");
		BrowserMobProxyServer ps = buildBMProxy(proxy);
		System.err.println(ps.getClientBindAddress());
		System.err.println(ps.getPort());
		Thread.sleep(100000);
	}

	@Test
	public void testScheduledTask() throws Exception {

		ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

		for(int i=0; i<1; i++) {

			ChromeDriverAgent agent = new ChromeDriverAgent();
			distributor.addAgent(agent);
		}

		// distributor.layout();

		/*ChromeTaskHolder holder = ChromeTask.buildHolder(
				one.rewind.io.requester.test.TestChromeTask.T4.class, ImmutableMap.of("q", String.valueOf(1950)));

		distributor.submit(holder);*/

		Thread.sleep(600000);

	}

	@Test
	public void testScanTask() throws Exception {

		ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

		for(int i=0; i<1; i++) {

			ChromeDriverAgent agent = new ChromeDriverAgent();
			distributor.addAgent(agent);
		}

		// distributor.layout();

		/*ChromeTaskHolder holder = ChromeTask.buildHolder(
				one.rewind.io.requester.test.TestChromeTask.T5.class, ImmutableMap.of("q", String.valueOf(1950), "max_page", 60));

		distributor.submit(holder);*/

		Thread.sleep(600000);

	}

	@Test
	public void testPostRequest() throws Exception {

		String url = "https://www.jfh.com/jfhrm/buinfo/showbucaseinfo";
		Map<String, String> data = ImmutableMap.of("uuidSecret", "MTQxODM7NDQ%3D");

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		ChromeTask task = new ChromeTask(url);
		task.addAction(new PostAction(url, data));
		agent.submit(task);
	}

	@Test
	public void testResponseFilter() throws Exception {

		String url = "https://www.mihuashi.com/users/Nianless?role=employer";

		//Proxy proxy = new ProxyImpl("10.0.0.56", 49999, null, null);
		ChromeDriverAgent agent = new ChromeDriverAgent(ChromeDriverAgent.Flag.MITM);

		agent.start();

		ChromeTask task = new ChromeTask(url);

		task.addAction(new ClickAction("#users-show > div.container-fluid > div.profile__container > main > header > ul > li:nth-child(2) > a", 1000));

		task.addAction(new LoadMoreContentAction("#vue-comments-app > div:nth-child(2) > a > span:nth-child(1)"));

		task.setResponseFilter((response, contents, messageInfo) -> {

			if(messageInfo.getOriginalUrl().matches(".*?/users/Nianless/comments\\?role=employer&per=\\d+&page=\\d+")) {
				task.getResponse().setVar("content",
						task.getResponse().getVar("content") == null?
								contents.getTextContents() :
								task.getResponse().getVar("content") + "\n" + contents.getTextContents());
			}
		});

		agent.submit(task);

		System.err.println(task.getResponse().getVar("content"));

		Thread.sleep(10000000);
	}
}

