package com.sdyk.ai.crawler.old_model.witkey;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import one.rewind.db.DBName;

/**
 * 服务
 */
@DBName(value = "sdyk_raw_")
@DatabaseTable(tableName = "cases")
public class Case extends Model {

	// 服务商id
	@DatabaseField(dataType = DataType.STRING, width = 32, index = true)
	public String user_id;

	// 名称
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String title;

	// 类型描述
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String category;

	// 标签
	@DatabaseField(dataType = DataType.STRING, width = 512)
	public String tags;

	// 描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	// 响应时间
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String response_time;

	// 响应时间
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String time_limit;

	// 服务态度
	@DatabaseField(dataType = DataType.DOUBLE)
	public double service_attitude;

	// 工作速度
	@DatabaseField(dataType = DataType.DOUBLE)
	public double service_speed;

	// 完成质量
	@DatabaseField(dataType = DataType.DOUBLE)
	public double service_quality;

	// 购买人数
	@DatabaseField(dataType = DataType.INTEGER)
	public int purchase_num;

	// 预算上限
	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget_ub;

	// 预算下限
	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget_lb;

	// 客户评分
	@DatabaseField(dataType = DataType.FLOAT)
	public float rating;

	// 客户评价数
	@DatabaseField(dataType = DataType.INTEGER)
	public int rate_num;

	// 附件
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String attachment_ids;

	public Case(){}

	public Case(String url) {
		super(url);
	}

	/**
	 * 插入ES
	 * @return
	 */
	public boolean insert() {

		super.insert();

		try {

			ServiceProvider serviceProvider = (ServiceProvider) getById(ServiceProvider.class, this.user_id);
			serviceProvider.fullfill();
			serviceProvider.updateES();

			return true;
		} catch (Exception e) {

			logger.error("Can not find ServiceProvider: {}", this.user_id, e);
			return false;
		}
	}
}
