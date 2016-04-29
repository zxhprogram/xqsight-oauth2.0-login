package net.dreamlu.api.oauth;


import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.dreamlu.api.util.TokenUtil;

/**
 * OauthQQ
 * email: 596392912@qq.com
 * site:  http://www.dreamlu.net
 * @author L.cm
 * @date Jun 24, 2013 9:35:23 PM
 */
public class OauthQQ extends Oauth{ 

	private static final Logger LOGGER = LogManager.getLogger(OauthQQ.class);
	
	private static final String AUTH_URL = "https://graph.qq.com/oauth2.0/authorize";
	private static final String TOKEN_URL = "https://graph.qq.com/oauth2.0/token";
	private static final String TOKEN_INFO_URL = "https://graph.qq.com/oauth2.0/me";
	private static final String USER_INFO_URL = "https://graph.qq.com/user/get_user_info";

	private static OauthQQ oauthQQ = new OauthQQ();

	/**
	 * 用于链式操作
	 * @return
	 */
	public static OauthQQ me() {
		return oauthQQ;
	}

	/**
	 * 获取授权url
	 * DOC：http://wiki.connect.qq.com/%E4%BD%BF%E7%94%A8authorization_code%E8%8E%B7%E5%8F%96access_token
	 * @param @return	设定文件
	 * @return String	返回类型
	 */ 
	public String getAuthorizeUrl(String state) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("response_type", "code");
		params.put("client_id", getClientId());
		params.put("redirect_uri", getRedirectUri());
		if (StringUtils.isBlank(state)) {
			params.put("state", state); //OAuth2.0标准协议建议，利用state参数来防止CSRF攻击。可存储于session或其他cache中
		}
		return super.getAuthorizeUrl(AUTH_URL, params);
	}

	/**
	 * 获取token
	 * @param @param code
	 * @param @return	设定文件
	 * @return String	返回类型
	 * @throws
	 */
	public String getTokenByCode(String code) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("code", code);
		params.put("client_id", getClientId());
		params.put("client_secret", getClientSecret());
		params.put("grant_type", "authorization_code");
		params.put("redirect_uri", getRedirectUri());
		// access_token=FE04************************CCE2&expires_in=7776000
		String token = TokenUtil.getAccessToken(super.doGet(TOKEN_URL, params));
		LOGGER.debug(token);
		return token;
	}

	/**
	 * 获取TokenInfo
	 * @return	设定文件
	 * @return String	返回类型
	 * @throws
	 */
	public String getTokenInfo(String accessToken) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", accessToken);
		// callback( {"client_id":"YOUR_APPID","openid":"YOUR_OPENID"} );
		String openid = TokenUtil.getOpenId(super.doGet(TOKEN_INFO_URL, params));
		LOGGER.debug(openid);
		return openid;
	}
	
	/**
	 * 获取用户信息
	 * DOC：http://wiki.connect.qq.com/get_user_info
	 * @param accessToken
	 * @param uid
	 * @return	设定文件
	 * @return String	返回类型
	 */
	public String getUserInfo(String accessToken, String uid) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", accessToken);
		params.put("oauth_consumer_key", getClientId());
		params.put("openid", uid);
		params.put("format", "json");
		// // {"ret":0,"msg":"","nickname":"YOUR_NICK_NAME",...}
		String userinfo = super.doGet(USER_INFO_URL, params);
		LOGGER.debug(userinfo);
		return userinfo;
	}
	
	/**
	 * 根据code一步获取用户信息
	 * @param @param args	设定文件
	 * @return void	返回类型
	 * @throws
	 */
	public JSONObject getUserInfoByCode(String code) {
		String accessToken = getTokenByCode(code);
		if (StringUtils.isBlank(accessToken)) {
			return null;
		}
		String openId = getTokenInfo(accessToken);
		if (StringUtils.isBlank(openId)) {
			return null;
		}
		JSONObject dataMap = JSON.parseObject(getUserInfo(accessToken, openId));
		dataMap.put("openid", openId);
		dataMap.put("access_token", accessToken);
		return dataMap;
	}
	
	@Override
	public Oauth getSelf() {
		return this;
	}
}
