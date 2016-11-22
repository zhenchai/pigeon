/**
 * 
 */
package com.dianping.pigeon.remoting.provider.config;


import com.dianping.pigeon.remoting.provider.config.spring.PoolBean;

/**
 * @author xiangwu
 * 
 */
public class ProviderMethodConfig {

	private String name;

	private int actives = 0;

	private PoolBean poolBean;

	public PoolBean getPoolBean() {
		return poolBean;
	}

	public void setPoolBean(PoolBean poolBean) {
		this.poolBean = poolBean;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getActives() {
		return actives;
	}

	public void setActives(int actives) {
		this.actives = actives;
	}

}
