package com.auto.common;

import org.openqa.selenium.By;

public interface PageElement {
    /**********************理财产品***********************/    
 // 断言-理财产品断言1,2,3,4,5,6,7,8,9,10,11,12,
    By FinancialAssert = By.xpath("//android.widget.TextView[@resource-id='cn.com.spdb.mobilebank.per:id/title_title_textview']");    
    // 理财产品    
    By FinancialButton = By.xpath("//android.support.v7.widget.RecyclerView[@resource-id='cn.com.spdb.mobilebank.per:id/rv_menu_list']/android.widget.RelativeLayout[6]/android.widget.LinearLayout[1]");
    //理财产品持仓登录
    By MyPositionFin = By.xpath("//android.widget.Button[@content-desc='登录']");
    //理财产品...
    By MyPositionInfo = By.xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/title_right_button']");
    //理财码
    By MyPositionQR = By.xpath("//android.widget.TextView[@resource-id='cn.com.spdb.mobilebank.per:id/item_finance_title_tv']");
    //签约信息
    By FinancialContract = By.xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/title_right_button']");
    //理财产品持有中
    By FinanicalHold = By.xpath("//android.webkit.WebView[@content-desc='理财产品持仓首页--我的持仓(专项理财产品)']/android.view.View[3]");
    //理财持仓产品
    By FinanicalInformation = By.xpath("//android.view.View[@content-desc='固定持有期(T+1) 4501415470']");
    //理财产品详情
    By FinanicalInfoProduct = By.xpath("//android.view.View[@content-desc='固定持有期(T+1) 4501415470']");
    //理财产品持仓明细
    By FinanicalInfoDetail = By.xpath("//android.widget.Button[@content-desc='持仓明细']");
    //理财产品再次买入
    By AgainBuyFinanical = By.xpath("//android.widget.Button[@content-desc='再次买入']");
    //产品适度评估
    By FinanicalRisk1 = By.xpath("//android.webkit.WebView[@content-desc='专项理财产品持仓详情']/android.widget.ListView[1]/android.view.View[2]/android.view.View[1]/android.widget.RadioButton[1]");
    By FinanicalRisk2 = By.xpath("//android.webkit.WebView[@content-desc='专项理财产品持仓详情']/android.widget.ListView[2]/android.view.View[2]/android.view.View[1]/android.widget.RadioButton[1]");
    By FinanicalRisk3 = By.xpath("//android.webkit.WebView[@content-desc='专项理财产品持仓详情']/android.widget.ListView[3]/android.view.View[2]/android.view.View[1]/android.widget.RadioButton[1]");
    //勾选评估的协议
    By FinanicalAgreeMent = By.xpath("//android.webkit.WebView[@content-desc='专项理财产品持仓详情']/android.view.View[1]");
    //评估提交
    By FinanicalSubmit = By.xpath("//android.widget.Button[@resource-id='select2']");
    //输入买入金额
    By FinanicalMoney = By.xpath("//android.widget.EditText[@resource-id='buyMoney']");
    //买入理财勾选协议
    By FinanicalAgreeMent1 = By.xpath("//android.view.View[@resource-id='pageA']/android.view.View[12]/android.view.View[1]");
    //理财购买提交
    By FinanicalBuySub = By.xpath("//android.widget.Button[@content-desc='提 交']");    
    //理财产品委托中
    By FinanicalProxy = By.xpath("//android.webkit.WebView[@content-desc='理财产品持仓首页--我的持仓(专项理财产品)']/android.view.View[5]");
    //理财产品委托中产品
    By FinanicalInformations = By.xpath("//android.view.View[@content-desc='净值类周期型18501181302']");
    //理财产品委托中产品详情
    By FinanicalInfoProducts = By.xpath("//android.view.View[@content-desc='净值类周期型18501181302']");
    //理财产品委托中产品点击撤单
    By FinanicalRevoke = By.xpath("//android.widget.Button[@content-desc='撤 单']");
    //理财产品已成交
    By FinanicalFinish = By.xpath("//android.webkit.WebView[@content-desc='理财产品持仓首页--我的持仓(专项理财产品)']/android.view.View[7]");
    //切换下拉列表任意条件
    By FinanicalChangeSelect = By.xpath("//android.view.View[@resource-id='cpid']");
    By FinanicalChangeSelect1 = By.xpath("//android.view.View[@resource-id='monthDetail']");
    //转让我的理财产品
    By FinanicalConvey = By.xpath("//android.view.View[@content-desc='转让我的理财产品']");
    //在线客服人员
    By FinanicalOnLineSerivce = By.xpath("//android.widget.LinearLayout[@resource-id='cn.com.spdb.mobilebank.per:id/dialog_customer_service_ll_online_customer']");
    //点击全部
    By FinanicalAll = By.xpath("//android.widget.Image[@content-desc='quanbu']");
    //全部中的任意产品
    By FinanicalAllOther = By.xpath("//android.webkit.WebView[@content-desc='理财产品..']/android.view.View[6]");
    //点击推荐
    By FinanicalDetailRecom = By.xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/title_right_button']");
    //点击生成营销二维码
    By FinanicalQRCode = By.xpath("//android.widget.Button[@resource-id='dtbtn']");
    //点击立即分享
    By FinanicalShare = By.xpath("//android.widget.Button[@content-desc='立即分享']");
    //点击收藏
    By FinanicalCollect = By.xpath("//android.view.View[@resource-id='collect']");
    //点击认购
    By FinanicalApply = By.xpath("//android.view.View[@resource-id='purch']");    
    //点击灵活
    By FinanicalQuick = By.xpath("//android.widget.Image[@content-desc='lh']");
    //点击中短期
    By FinanicalShortMT = By.xpath("//android.widget.Image[@content-desc='zdq']");
    //点击长期
    By FinanicalLongT = By.xpath("//android.widget.Image[@content-desc='cq']");
    //点击保本
    By FinanicalCapitalG = By.xpath("//android.widget.Image[@content-desc='bb']");
    //点击专属
    By FinanicalExclusive = By.xpath("//android.widget.Image[@content-desc='zs']");
    //点击自选
    By FinanicalOptional = By.xpath("//android.widget.Image[@content-desc='zx']");
    //搜索
    By FinanicalSerach = By.xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/title_right_button2']");
    By FinanicalInputInfo = By.xpath("//android.widget.EditText[@resource-id='fundName']");
    By FinanicalSerachs = By.xpath("//android.view.View[@content-desc='搜索']");
    //常见问题
    By FinanicalQuestion = By.xpath("//android.widget.Image[@content-desc='wenhao']"); 
    //我的自选
    By MyOptional = By.xpath("//android.view.View[@content-desc='天添盈1号']");
    //灵活理财
    By MyQuick = By.xpath("//android.view.View[@content-desc='灵活理财']");
    //中短期理财
    By MyShortMT = By.xpath("//android.view.View[@content-desc='中短期理财']");
    //长期理财
    By MyLongT = By.xpath("//android.view.View[@content-desc='长期理财']");
    
    //理财转让
    By MyConvey = By.xpath("//android.view.View[@content-desc='理财转让']");
  //撮合转让
    By MyBuyThings = By.xpath("//android.webkit.WebView[@content-desc='理财转让']/android.view.View[9]/android.view.View[1]");
    
    //我要买入理财数据
    By MyBuyThing = By.xpath("//android.webkit.WebView[@content-desc='理财转让']/android.view.View[8]/android.view.View[1]");
    //输入买入金额
    By MyBuyThingMoney = By.xpath("//android.widget.EditText[@resource-id='redeem1']");
    //买入协议
    By MyBuyThingAgreeMent = By.xpath("//android.webkit.WebView[@content-desc='理财转让']/android.view.View[10]");
    //确认购买
    By MyBuySubmit = By.xpath("//android.widget.Button[@resource-id='qrgm']");
    //确认（是），继续
    By MyBuyGoOn = By.xpath("//android.widget.Button[@resource-id='jixu']");
    //确认交易信息
    By MyBuyOk = By.xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/dialog_confirm_info_btn_pay']");
        
    //定价转让
    By MyDJConvey = By.xpath("//android.view.View[@content-desc='定价转让']");
  //定价转让理财数据
    By MyDJBuyThing = By.xpath("//android.webkit.WebView[@content-desc='理财转让']/android.view.View[7]/android.view.View[1]");
  //确认购买
    By MyDJBuySubmit = By.xpath("//android.widget.Button[@resource-id='fbsqrgm']");
    
    //我要卖出
    By MyConveyWYMC = By.xpath("//android.view.View[@content-desc='我要卖出']");
    //我要卖出理财数据
    By MyConveyWYMCThings = By.xpath("//android.view.View[@content-desc='固定持有期(T+1)']");
    //卖出协议
    By MyConveyWYMCThingAgreeMent = By.xpath("//android.webkit.WebView[@content-desc='理财转让']/android.view.View[21]");
    //确认转让
    By MyConveyWYMCSubmit = By.xpath("//android.widget.Button[@resource-id='qrzr']");
    
    //交易查询
    By MyConveyJYCX = By.xpath("//android.view.View[@content-desc='交易查询']");
    
    //交易查询-委托类型
    By MyConveyWTType = By.xpath("//android.widget.Spinner[@resource-id='wtType']");
    By MyConveyWTTypeAll = By.xpath("//android.widget.CheckedTextView[@resource-id='android:id/text1' and @text='全部']");
    //交易查询-委托状态
    By MyConveyWTStatus = By.xpath("//android.widget.Spinner[@resource-id='wtsSatus']");
    By MyConveyWTStatusAll = By.xpath("//android.widget.CheckedTextView[@text='全部']");
    
    //交易查询-成交查询
    By MyConveyCJCX = By.xpath("//android.view.View[@content-desc='成交查询']");
    //交易查询-成交类型
    By MyConveyCJCXType = By.xpath("//android.widget.Spinner[@resource-id='cjType']");
    By MyConveyCJCXTypeAll = By.xpath("//android.widget.Spinner[@resource-id='cjType']");
    
    //交易查询-按钮
    By MyConveyWTQuery = By.xpath("//android.widget.Button[@resource-id='queryWT']");
    By MyConveyCJQuery = By.xpath("//android.widget.Button[@resource-id='queryCJ']");
    
    /**********************基金*************************/
    // 首页-全部
    By Menu_All = By.xpath("//android.widget.TextView[@text='全部']");
    // 首页-全部-财富-基金  
    By FundButton = By.xpath(
            "//android.support.v7.widget.RecyclerView[@resource-id='cn.com.spdb.mobilebank.per:id/rv_menu_list']/android.widget.RelativeLayout[7]/android.widget.LinearLayout[1]/android.widget.TextView[1]");
    // 首页-全部-财富-基金-我的持仓
    By MyPosition = By.xpath("//android.view.View[@content-desc='我的持仓']");
    // 断言-基金用例1,2,4,5,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42
    By FundAssert = By.xpath("//android.widget.TextView[@resource-id='cn.com.spdb.mobilebank.per:id/title_title_textview']");
    //首页-全部-财富-基金-我的持仓-常见问题
    By QuestionInfo = By.xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/title_right_button']");
    //首页-全部-财富-基金-我的持仓-所有卡
    By AllCardInfo = By.xpath("//android.widget.Spinner[@resource-id='fundCardA']");
    //断言-基金用例3
    By AllCardAssert = By.xpath("//android.widget.CheckedTextView[@text='所有卡']");
    //首页-全部-财富-基金-我的持仓-持仓产品详情
    By FundPositionDe = By.xpath("//android.view.View[@content-desc='金鹰优选210001']");
    //~~~持仓产品详情
    By FundPositionDet = By.xpath("//android.view.View[@content-desc='基金详情']");
    //首页-全部-财富-基金-我的持仓-持仓详情-客户服务
    By AcctService = By.xpath("//android.widget.Image[@content-desc='rentou']");
    //~~~持仓详情-客户服务-在线服务
    By OnLineService = By.xpath("//android.widget.TextView[@text='文字在线']");
    //断言-基金持仓客户服务用例6
    By AcctServiceAssert = By.xpath("//android.widget.ListView[@resource-id='cn.com.spdb.mobilebank.per:id/list']/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]");
    //~~~持仓详情-赎回
    By PositionRedeem = By.xpath("//android.view.View[@content-desc='赎回']");
    //~~~持仓详情-赎回-赎回份额（定位器）
    By RedeemNumber = By.xpath("//android.widget.EditText[@resource-id='FundCanUseShare2']");
    //~~~持仓详情-赎回-提交
    By RedeemSubmit = By.xpath("//android.widget.Button[@content-desc='提交']");
    //~~~持仓详情-赎回-提交输入交易密码（定位器）
    By RedeemTransactionPwd = By.xpath("//android.widget.EditText[@resource-id='cn.com.spdb.mobilebank.per:id/transfer_Pwd']");
    //~~~持仓详情-赎回-输入交易密码之后点击确定
    By RedeemOk = By.xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/transfer_acct']");
    //断言-基金持仓详情赎回用例7
    By PosRedeemAssert = By.xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/positiveButton']");
    By SystitleInfo = By.xpath("//android.widget.TextView[@resource-id='cn.com.spdb.mobilebank.per:id/title']");
    //~~~持仓详情-再次购买
    By AgainBuyPosition = By.xpath("//android.view.View[@content-desc='再次买入']");
    //~~~风险评估同意协议
    By AgreeMent = By.xpath("//android.webkit.WebView[@content-desc='投资风险评估']/android.view.View[168]");
    //~~~风险评估提交
    By RiskAssessOk = By.xpath("//android.view.View[@resource-id='submit']");
    //~~~风险评估完成
    By RiskAssessFinsh = By.xpath("//android.view.View[@content-desc='完成']");
    //~~~输入买入金额
    By FundBuyMoney = By.xpath("//android.widget.EditText[@resource-id='goumai_id']");
    //~~~勾选协议
    By FundAgreeMent = By.xpath("//android.widget.CheckBox[@resource-id='check-4']");
    By FundAgreeMents = By.xpath("//android.widget.CheckBox[@resource-id='checkcl']");
    //~~~买入基金提交
    By FundBuySubmit = By.xpath("//android.widget.Button[@resource-id='submitId']");   
    //断言-基金买入成功用例8
    By FundBuySuccess = By.xpath("//android.widget.TextView[@resource-id='cn.com.spdb.mobilebank.per:id/tv_fund_result_cue_words']");
    //~~~持仓详情-更多
    By FundMore = By.xpath("//android.view.View[@content-desc='更多']");    
    //~~~持仓详情-更多-定期定额申购
    By ApplyForThePosition = By.xpath("//android.view.View[@resource-id='dg']");
    //~~~输入定投金额
    By ThePositionMoney = By.xpath("//android.widget.EditText[@resource-id='TranAmt']");
    //~~~定投金额勾选协议
    By ThePosAgreeMent = By.xpath("//android.widget.CheckBox[@resource-id='check1']");
    //~~~定投提交
    By ThePositionSubmit = By.xpath("//android.widget.Button[@resource-id='bottonText']");
    //断言-定投申购成功用例10
    By ThePositionAssert = By.xpath("//android.view.View[@content-desc='您的基金定投已签约成功']");    
    //~~~持仓详情-更多-定期定额赎回
    By ApplyForTheRedeem = By.xpath("//android.view.View[@resource-id='ds']");
    //~~~定期定额赎回份额
    By TheRedeemNumber = By.xpath("//android.widget.EditText[@resource-id='AppAmnt']");
    //~~~基金赎回协议
    By TheRedeemAgreeMent = By.xpath("//android.webkit.WebView[@content-desc='基金签约定赎赎回']/android.view.View[6]");
    
    //登录成功之后返回
    By LoginSussessBack = By.xpath("//android.widget.LinearLayout[@resource-id='cn.com.spdb.mobilebank.per:id/title_left_layout']");
    //~~~基金超市
    By FundMart = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[2]");
    //~~~基金超市-全部基金
    By FundMartAll = By.xpath("//android.view.View[@content-desc='全部基金']"); 
    //~~~基金超市-可定投基金
    By FundMartCan = By.xpath("//android.view.View[@content-desc='可定投']");
    //~~~基金超市-筛选
    By FundMartSelect = By.xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/title_right_button']");
    //~~~基金产品详情
    By FundInfoDetail = By.xpath("//android.view.View[@content-desc='招商中证白酒指数分级证券投...']");
    //~~~基金产品详情收藏
    By FundInfoDetailCollect = By.xpath("//android.view.View[@resource-id='focusFlag']");
    //断言-收藏用例
    By FundInfoDetailCollectAssert = By.xpath("//android.view.View[@resource-id='focusFlag']");
    //~~~基金产品详情推荐
    By FundInfoDetailRecom = By.xpath("//android.widget.Image[@content-desc='share_CZZH']");
    //断言-推荐用例
    By FundInfoDetailRecomAssert = By.xpath("//android.view.View[@content-desc='推荐理由确认']");
    //~~~基金产品详情-推荐生成营销二维码
    By FundInfoQRCode = By.xpath("//android.widget.Button[@resource-id='dtbtn']");
    //断言-生成二维码用例
    By FundInfoQRCodeAssert = By.xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/btn_save']");
    //~~~基金产品详情-推荐立即分享
    By FundInfoShare = By.xpath("//android.widget.Button[@content-desc='立即分享']");
    //断言-推荐立即分享用例
    By FundInfoShareAssert = By.xpath("//android.view.View[@content-desc='微信好友']");
    //~~~基金产品详情-客户服务
    By FundInfoService = By.xpath("//android.webkit.WebView[@content-desc='Insert title here']/android.view.View[15]/android.view.View[1]");
    //断言-客户服务用例
    By FundInfoServiceAssert = By.xpath("//android.widget.TextView[@text='在线客服人员']");
    //~~~基金产品详情定投
    By FundInfoPlan = By.xpath("//android.view.View[@content-desc='定投']");

    //~~~基金产品详情申购
    By FundInfoApplyFor = By.xpath("//android.view.View[@resource-id='jjgm']");
        
    //~~~基金定投
    By FundPlanInfo = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[4]");
    //~~~基金定投排行榜
    By FundPlanChart = By.xpath("//android.view.View[@content-desc='定投排行榜']");    
    //首页-全部-财富-基金-基金比较
    By FundContrast = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[6]");
    //首页-全部-财富-基金-基金精选
    By ChoicesFund = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[8]");
    //首页-全部-财富-基金-我的持仓
    By FmyPosition = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[10]");
    //首页-全部-财富-基金-我的定投
    By MyInvestment = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[12]");
    //首页-全部-财富-基金-我的交易
    By MyTransaction = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[14]");
    //首页-全部-财富-基金-签约信息
    By ContractInfo = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[16]");
    //首页-全部-财富-基金-签约信息-修改账户信息
    By AlterAcctInfo = By.xpath("//android.view.View[@content-desc='修改账户信息']");
    //首页-全部-财富-基金-签约信息-查看TA账户
    By OtherAcctInfo = By.xpath("//android.view.View[@content-desc='查看TA账户']");
    //首页-全部-财富-基金-签约信息-账号交易查询
    By AcctTransaction = By.xpath("//android.view.View[@content-desc='账户交易查询']");
    //首页-全部-财富-基金-热点投资
    By HotInformations = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[21]");
    //首页-全部-财富-基金-热点投资-浦发优选
    By FundBestChoice = By.xpath("//android.view.View[@content-desc='浦发优选—优中选优，基金精品']");
    //首页-全部-财富-基金-精选基金
    By FundChoices = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[35]");
    //首页-全部-财富-基金-排行榜
    By FundCharts = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[37]");
    //首页-全部-财富-基金-新发基金
    By NewsFund = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[41]");
    //首页-全部-财富-基金-投基技巧
    By FundSkill = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[45]");
    //首页-全部-财富-基金-基金咨询
    By FundInformation = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[47]");
    //首页-全部-财富-基金-基金公司动态
    By FundCompany = By.xpath("//android.webkit.WebView[@content-desc='基金首页']/android.view.View[53]");
    
    /*----------------------风评-进取型投资人士答题----------------------*/
    By RiskAssess_Success = By.xpath("//android.view.View[@content-desc='个人客户投资风险评估结果']");
    By RiskAssess_Submit = By.xpath("//android.view.View[@content-desc='提交']");
    By RiskAssess_Ensure = By.xpath("//android.view.View[167]/android.view.View[168]");
    By RiskAssess_016_E = By.xpath("//android.view.View[@resource-id='016_E']");
    By RiskAssess_015_E = By.xpath("//android.view.View[@resource-id='015_E']");
    By RiskAssess_014_C = By.xpath("//android.view.View[@resource-id='014_C']");
    By RiskAssess_013_D = By.xpath("//android.view.View[@resource-id='013_D']");
    By RiskAssess_012_C = By.xpath("//android.view.View[@resource-id='012_C']");
    By RiskAssess_011_D = By.xpath("//android.view.View[@resource-id='011_D']");
    By RiskAssess_010_D = By.xpath("//android.view.View[@resource-id='010_D']");
    By RiskAssess_09_E = By.xpath("//android.view.View[@resource-id='09_E']");
    By RiskAssess_08_C = By.xpath("//android.view.View[@resource-id='08_C']");
    By RiskAssess_07_E = By.xpath("//android.view.View[@resource-id='07_E']");
    By RiskAssess_06_A = By.xpath("//android.view.View[@resource-id='06_A']");
    By RiskAssess_05_D = By.xpath("//android.view.View[@resource-id='05_D']");
    By RiskAssess_04_F = By.xpath("//android.view.View[@resource-id='04_F']");
    By RiskAssess_03_E = By.xpath("//android.view.View[@resource-id='03_E']");
    By RiskAssess_02_C = By.xpath("//android.view.View[@resource-id='02_C']");
    By RiskAssess_Title = By.xpath("//android.view.View[@content-desc='上海浦东发展银行']");
    // 我的风险评估-过期-下一步
    By RiskAssess_NextStep = By.xpath("//android.widget.Button[@content-desc='下一步']");
    // 我的风险评估-过期-确定
    By RiskAssess_ExpireFix = By
            .xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/positiveButton']");
    // 我的风险评估-重新评估
    By RiskAssess_Reappraise = By.xpath("//android.view.View[@content-desc='重新评估']");
    // 财富-风险评估
    By Treasure_RiskAssess = By.xpath(
            "//android.support.v7.widget.RecyclerView[@resource-id='cn.com.spdb.mobilebank.per:id/rv_menu_list']/android.widget.RelativeLayout[14]/android.widget.LinearLayout[1]/android.widget.ImageView[1]");
    // 全部-财富
    By All_Treasure = By.xpath(
            "//android.widget.ListView[@resource-id='cn.com.spdb.mobilebank.per:id/lv_navigation_list']/android.widget.LinearLayout[2]/android.widget.LinearLayout[1]/android.widget.TextView[1]");
    // 首页-全部
    By Home_All = By.xpath(
            "//android.widget.GridView[@resource-id='cn.com.spdb.mobilebank.per:id/gv_menu_second_line']/android.widget.RelativeLayout[10]/android.widget.LinearLayout[1]/android.widget.ImageView[1]");

    // 断言-我的账户用例4
    By Assert_AccountCredit = By.xpath("//android.view.View[@content-desc='关闭页面']");
    // 断言-转账用例22
    By Assert_TransferAppointBackOut = By.xpath("//android.view.View[@content-desc='汇款编号']");
    // 断言-转账用例15
    By Assert_TransferWechatRemits = By.xpath("//android.view.View[@resource-id='wujilu']");
    // 断言-转账用例16(无记录)
    By wujilu = By.xpath("//android.view.View[@resource-id='wujilu']");
    // 断言-转账用例14/转账用例16
    By Assert_TransferWechatPhoneDown = By.xpath(
            "//android.widget.RelativeLayout[@resource-id='cn.com.spdb.mobilebank.per:id/img_layout']/android.widget.ImageView[1]");
    // 断言-转账用例9
    By Assert_TransferWechatRemit = By.xpath("//android.widget.Button[@content-desc='重发']");
    // 断言-转账用例4
    By Assert_TransferInterest = By.xpath("//android.view.View[@resource-id='purch']");
    // 断言-转账用例1/转账用例12
    By Assert_BankTransfer = By.xpath("//android.widget.Image[@resource-id='sucessimg']");
    // 断言-登陆用例4
    By Assert_AccountLogin = By.xpath("//android.view.View[@resource-id='listBank']");
    // 断言-登陆用例3/转账用例2/转账用例5/转账用例13
    By Assert_TransferLogin = By.xpath("//android.view.View[@content-desc='收款人']");
    // 断言-登陆用例2
    By Assert_MyMenu = By.xpath("//android.view.View[@content-desc=' 我的收支']");
    // 断言-登陆用例1
    By Assert_OpenSpdb = By.xpath("//android.widget.TextView[@text='转账汇款']");
    // 账户总览-申请信用卡
    By CreditCard = By.xpath("//android.view.View[@resource-id='CreditCard']");
    // 筛选条件-重置/断言-我的账户用例2
    By Screen_reset = By.xpath("//android.view.View[@content-desc='重置']");
    // 筛选条件-历史支付
    By HPD = By.xpath("//android.view.View[@resource-id='HPD']");
    // 筛选条件-确定
    By Screen_ok = By.xpath("//android.view.View[@content-desc='确定']");
    // 借记卡(8433)-筛选
    By Screen = By.xpath("//android.view.View[@content-desc='筛选']");
    // 借记卡(8433)-卡列表-借记卡(8433)
    By SelectCard_8433 = By.xpath("//android.view.View[@content-desc='6217 **** 0976']");
    // 借记卡(0976)-卡列表-借记卡(0976)
    By SelectCard_0976 = By.xpath("//android.widget.CheckedTextView[@text='三类账户 (0976)']");
    // 借记卡(8433)-下拉框
    By SelectCard = By.xpath("//android.widget.Spinner[@resource-id='selectCard']");
    // 借记卡(8433)-睁开的眼睛
    By Zhengyan = By.xpath("//android.widget.Image[@resource-id='zhengyan']");
    // 借记卡(8433)-关闭的眼睛
    By Biyan = By.xpath("//android.widget.Image[@resource-id='biyan']");
    // 账户总览-尾号8433II类
    By Debit_card_8433 = By.xpath("//android.view.View[@content-desc='尾号0976  Ⅲ 类']");
    // 账户总览-尾号0976
    By Debit_card_0976 = By.xpath("//android.view.View[@content-desc='尾号0976  ']");
    // 温馨提示-确定
    By ReminderButton = By
            .xpath("//android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/positiveButton']");
    // 我的账户-总负债-问号
    By Liabilities_question = By.xpath("//android.view.View[@resource-id='balance2']/android.widget.Image[1]");
    // 我的账户-总资产-问号
    By Deposit_question = By.xpath("//android.view.View[@resource-id='balance0']/android.widget.Image[1]");
    // 我的账户-总负债/断言-我的账户用例1/我的账户用例3
    By Sum_liabilities = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.support.v4.widget.DrawerLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.webkit.WebView[1]/android.webkit.WebView[1]/android.view.View[8]");
    // 我的账户-总资产
    By Sum_deposit = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.support.v4.widget.DrawerLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.webkit.WebView[1]/android.webkit.WebView[1]/android.view.View[4]");
    // 我的账户-总负债
    By Total_liabilities = By.xpath(
            " //android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.support.v4.widget.DrawerLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.webkit.WebView[1]/android.webkit.WebView[1]/android.view.View[3]/android.view.View[2]");
    // 我的账户-总存款
    By Total_Deposits = By.xpath("//android.view.View[@resource-id='balance0']/android.view.View[2]");
    // 账户总览-蓝色底色
    By Header_blue = By.xpath("//android.view.View[@resource-id='header']/android.view.View[2]");
    // 账户总览-睁开的眼睛
    By OpenEye = By.xpath("//android.widget.Image[@resource-id='openEye']");
    // 账户总览-关闭的眼睛
    By CloseEye = By.xpath("//android.widget.Image[@resource-id='closeEye']");
    // 约定汇款-约定跨行汇款
    By Afflux_tab = By.xpath("//android.view.View[@resource-id='hklx']/android.view.View[2]");
    // 约定汇款-汇款规则查询、修改及撤销
    By Appoint_backout = By.xpath("//android.view.View[@content-desc='汇款规则查询、修改及撤销']");
    // 添加收款人-确定
    By Ensure = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.support.v4.widget.DrawerLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.webkit.WebView[1]/android.webkit.WebView[1]/android.view.View[1]/android.widget.Button[1]");
    // 约定汇款-选择收款网点-选择网点
    By Branch_first = By.xpath("//android.view.View[@content-desc='中国工商银行总行营业部']");
    // 约定汇款-选择收款网点-搜索
    By Branch_search = By.xpath("//android.view.View[@content-desc='搜索']");
    // 约定汇款-选择收款网点-搜索栏
    By DeptName = By.xpath("//android.widget.EditText[@resource-id='DeptName']");
    // 选择收款网点第一条
    By PayeeFirst = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.support.v4.widget.DrawerLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.webkit.WebView[1]/android.webkit.WebView[1]/android.widget.ListView[1]/android.view.View[1]/android.view.View[1]");
    // 添加收款人-收款网点
    By Can_not_click = By.xpath("//android.view.View[@content-desc='可不选']");
    // 约定汇款-网点城市-确定
    By btnSubmit = By.id("cn.com.spdb.mobilebank.per:id/btnSubmit");
    // 约定汇款-收款网点
    By Receipt_branch = By.xpath("//android.view.View[@content-desc='请选择收款网点']");
    // 约定汇款-网点城市
    By Branch_city = By.xpath("//android.view.View[@content-desc='请选择城市']");
    // 约定汇款-转给其他账户/断言-是否位于"转账汇款"页面
    By Other_account = By.xpath("//android.view.View[@content-desc='转给其他账户']");
    // 约定汇款-收款账号
    By PayeeAcctNo = By.xpath("//android.widget.EditText[@resource-id='PayeeAcctNo']");
    // 约定汇款-添加新的收款人
    By Add_payee = By.xpath("//android.view.View[@content-desc='添加新的收款人']");
    // 约定汇款-管理
    By AppointManage = By.xpath("//android.view.View[@content-desc='管理']");
    // 约定汇款-第一条它行银行卡
    By Other_cards = By.xpath("//android.widget.ListView[@resource-id='registerList']/android.view.View[2]");
//    // 约定汇款-它行银行卡
//    By Other_card1 = By.xpath("//android.widget.ListView[@resource-id='registerList']/android.view.View[2]");
    // 约定汇款-每年
    By Every_years = By.xpath("//android.widget.CheckedTextView[@text='每年']");
    // 约定汇款-汇款频率
    By RemitFre = By.xpath("//android.widget.Spinner[@resource-id='RemitFre']");
    // 转账汇款-约定汇款
    By Appoint = By.xpath("//android.view.View[@content-desc='约定汇款']");
    // 普通/次日汇款-动态密码
    By ChooseDynamicPassword = By
            .xpath("//android.widget.CheckedTextView[@resource-id='android:id/text1' and @text='动态密码']");
    // 普通/次日汇款-次日发起
    By Morrow_start = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.support.v4.widget.DrawerLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.webkit.WebView[1]/android.webkit.WebView[1]/android.view.View[10]/android.view.View[1]");
    // 普通/次日汇款-普通发起
    By Common_start = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.support.v4.widget.DrawerLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.webkit.WebView[1]/android.webkit.WebView[1]/android.view.View[8]/android.view.View[1]");
    // 普通/次日汇款-普通发起
    By Ordinary_remittance = By.xpath("//android.view.View[@resource-id='pageInput']/android.view.View[7]");
    // 转账汇款-普通/次日汇款
    By Common_remittance = By.xpath("//android.view.View[@content-desc='普通/次日汇款']");
    // 汇款/转账查询-1个月/断言-转账汇款15
    By One_month = By.xpath("//android.widget.Button[@content-desc='1个月']");
    // 汇款/转账查询-3个月
    By Three_month = By.xpath("//android.widget.Button[@content-desc='3个月']");
    // 汇款/转账查询-详情
    By Query_details = By.xpath("//android.view.View[@content-desc='详情']");
    // 汇款/转账查询-汇入
    By Afflux_tab1 = By.xpath("//android.view.View[@resource-id='tab1']");
    // 汇款/转账查询-汇出
    By Remit_tab0 = By.xpath("//android.view.View[@resource-id='tab0']");
    // 汇款/转账查询-日减少
    By Day_sub = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.FrameLayout[1]/android.widget.DatePicker[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.NumberPicker[3]/android.widget.Button[1]");
    // 汇款/转账查询-日增加
    By Day_add = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.FrameLayout[1]/android.widget.DatePicker[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.NumberPicker[3]/android.widget.Button[2]");
    // 汇款/转账查询-月减少
    By Month_sub = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.FrameLayout[1]/android.widget.DatePicker[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.NumberPicker[2]/android.widget.Button[1]");
    // 汇款/转账查询-月增加
    By Month_add = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.FrameLayout[1]/android.widget.DatePicker[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.NumberPicker[2]/android.widget.Button[2]");
    // 汇款/转账查询-年减少
    By Year_sub = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.FrameLayout[1]/android.widget.DatePicker[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.NumberPicker[1]/android.widget.Button[1]");
    // 汇款/转账查询-年增加
    By Year_add = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.FrameLayout[1]/android.widget.DatePicker[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.NumberPicker[1]/android.widget.Button[2]");
    // 汇款/转账查询-结束日期
    By Enddate = By.xpath("//android.widget.EditText[@resource-id='enddate']");
    // 汇款/转账查询-起始日期
    By Startdate = By.xpath("//android.widget.EditText[@resource-id='startdate']");
    // 汇款/转账查询-尾号8433卡
    By AcceptAcctNo_8433 = By
            .xpath("//android.widget.CheckedTextView[@resource-id='android:id/text1' and @text='‎6217 **** 0976‎']");
    // 汇款/转账查询-卡/折
    By AcceptAcctNo = By.xpath("//android.widget.Spinner[@resource-id='AcceptAcctNo']");
    // 转账汇款-汇款/转账查询
    By Transfer_query = By.xpath("//android.view.View[@content-desc='汇款/转账查询']");
    // 汇款/转账查询-系统提示-文本信息
    By message = By.id("cn.com.spdb.mobilebank.per:id/message");
    // 手机汇款汇款回单-分享到微信
    By sharewx_popbtn = By.id("cn.com.spdb.mobilebank.per:id/sharewx_popbtn");
    // 手机汇款汇款回单-保存到手机
    By savepic_popbtn = By.id("cn.com.spdb.mobilebank.per:id/savepic_popbtn");
    // 转账结果-下载汇款回单
    By bt_remittance_succeed_receipt = By.id("cn.com.spdb.mobilebank.per:id/bt_remittance_succeed_receipt");
    By bt_remittance_succeed_receipt2 = By
            .xpath(" //android.widget.Button[@resource-id='cn.com.spdb.mobilebank.per:id/title_right_button']");
    // 手机号汇款-请输入交易密码-默认弹出键盘
    By keyboard_view = By.xpath("//android.view.View[@resource-id='cn.com.spdb.mobilebank.per:id/keyboard_view']");
    // 手机号汇款-请输入交易密码-交易密码
    By transfer_Pwd = By.id("cn.com.spdb.mobilebank.per:id/transfer_Pwd");
    // 手机号汇款-请输入交易密码-动态密码
    By transfer_dynamic = By.id("cn.com.spdb.mobilebank.per:id/transfer_dynamic");
    // 手机号汇款-认证方式-动态密码
    By DynamicPassword = By.id("android:id/text1");
    // 手机号汇款-认证方式
    By Dev_certContent = By.xpath("//android.widget.Spinner[@resource-id='dev_certContent']");
    // 手机号汇款-手机号
    By PayeeAlias = By.xpath("//android.widget.EditText[@resource-id='PayeeAlias']");
    // 手机号汇款
    By Transfer_phone = By.xpath("//android.view.View[@content-desc='手机号汇款']");
    // 微信查询-查询详情-撤销
    By Transfer_back_out = By.xpath("//android.widget.Button[@content-desc='撤 销']");
    // 微信查询-查询列表-汇款信息
    By Transfer_message_1 = By
            .xpath("//android.view.View[@resource-id='registerList']/android.widget.ListView[1]/android.view.View[1]");
    // 微信查询-查询
    By Wechat_query = By.xpath("//android.widget.Button[@content-desc='查 询']");
    // 微信查询-终止日期-确定/小米权限允许
    By EndDateConfirm = By.xpath("//android.widget.Button[@resource-id='android:id/button1']");
    // 微信查询-起始日期-取消
    By BeginDateCancel = By.xpath("//android.widget.Button[@resource-id='android:id/button2']");
    // 微信查询-终止日期
    By EndDate = By.xpath("//android.widget.EditText[@resource-id='EndDate']");
    // 微信查询-起始日期
    By BeginDate = By.xpath("//android.widget.EditText[@resource-id='BeginDate']");
    // 微信汇款-兑付密码
    By ReservePass = By.xpath("//android.widget.EditText[@resource-id='ReservePass']");
    // 微信汇款-预留兑付密码
    By Cash_password = By.xpath("  //android.view.View/android.view.View[2]/android.view.View[1]");
    By Cash_password1 = By.xpath(
            "//android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.support.v4.widget.DrawerLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.webkit.WebView[1]/android.webkit.WebView[1]/android.widget.ListView[1]/android.view.View[3]/android.view.View[2]/android.view.View[1]/android.view.View[2]");
    // 微信汇款确认-下一步
    By NextStep = By.xpath("//android.widget.Button[@content-desc='下一步']");
    // 微信汇款确认-交易密码
    By TranPwd = By.xpath("//android.widget.EditText[@resource-id='tranPwd']");
    // 微信汇款确认-动态密码
    By MobilePassword = By.xpath("//android.widget.EditText[@resource-id='mobilePassword']");
    // 微信汇款-附言
    By Note = By.xpath("//android.widget.EditText[@resource-id='Note']");
    // 微信汇款-收款人姓名
    By PayeeName = By.xpath("//android.widget.EditText[@resource-id='PayeeName']");
    // 微信汇款
    By Transfer_wechat = By.xpath("//android.view.View[@content-desc='微信汇款']");
    // 活期转活期-备注说明/断言-转账用例6
    By Transfer_note = By.xpath("//android.view.View[@content-desc='活期转活期收费费率说明']");
    // 系统提示-确定/首次登陆-允许使用位置信息/断言-转账用例7/转账用例8/转账用例10/转账用例11
    By positiveButton = By.id("cn.com.spdb.mobilebank.per:id/positiveButton");
    // 转存金额
    By Save_money = By.xpath("//android.widget.EditText[@resource-id='save_money']");
    // 转入银行卡
    By IncomeId = By.xpath("//android.view.View[@resource-id='incomeId']");
    // 转出银行卡
    By OutcomeId = By.xpath("//android.view.View[@resource-id='outcomeId']");
    // 其他转账方式-本人名下互转
    By My_bank_transfer = By.xpath("//android.view.View[@content-desc='本人名下互转']");
    // 转账汇款-"+"菜单
    By title_right_button = By.id("cn.com.spdb.mobilebank.per:id/title_right_button");
    // 可能感兴趣1号产品
    By Tv_remittance_succeed_product_name_01 = By.xpath(
            "//android.widget.ListView[@resource-id='cn.com.spdb.mobilebank.per:id/lv_remittance_hot']/android.widget.LinearLayout[1]");
    // 继续向TA转账
    By bt_continue_trans = By.id("cn.com.spdb.mobilebank.per:id/bt_continue_trans");
    // 尾号8433储蓄卡
    By Deposit_card_8433 = By.xpath("//android.view.View[@content-desc='6217 **** 0976']");
    // 借记卡(0837)
    By Card_desc = By.xpath("//android.view.View[@resource-id='cardDesc']");
    // 完成/断言-转账用例17
    By bt_remittance_succeed_ok = By.id("cn.com.spdb.mobilebank.per:id/bt_remittance_succeed_ok");
    // 确认转账/断言-转账用例3
    By Confirm_transfer = By.xpath("//android.widget.Button[@content-desc='向TA再转一笔']");
    // 转账(确定)
    By transfer_acct = By.id("cn.com.spdb.mobilebank.per:id/transfer_acct");
    // 确认转账/断言-转账用例18/转账用例19
    By Confirm_the_transfer = By.xpath("//android.view.View[@content-desc='确认转账']");
    // 汇款附言
    By PostScript = By.xpath("//android.widget.EditText[@resource-id='PostScript']");
    // 添加收款人-网点城市
    By Remit_city = By.xpath("//android.view.View[@resource-id='Remit']/android.view.View[2]");
    // 确认汇款金额
    By digitkeypad_ok = By.id("cn.com.spdb.mobilebank.per:id/digitkeypad_ok");
    // 输入汇款金额
    By digitpadedittext = By.id("cn.com.spdb.mobilebank.per:id/digitpadedittext");
    // 汇款金额
    By Amount = By.xpath("//android.widget.EditText[@resource-id='Amount']");
    // 转出卡
    By tv_guide_bottom_2 = By.id("cn.com.spdb.mobilebank.per:id/tv_guide_bottom_2");
    // 尾号0837储蓄卡
    By Deposit_card_0837 = By.xpath("//android.view.View[@content-desc='6217 **** 3987']");
    // 收款人栏下拉按钮
    By Pull_down = By.xpath("//android.widget.Image[@content-desc='down']");
    // 我的账户
    By Account = By.xpath(
            "//android.widget.GridView[@resource-id='cn.com.spdb.mobilebank.per:id/gv_menu_first_line']/android.widget.RelativeLayout[1]/android.widget.LinearLayout[1]/android.widget.ImageView[1]");
    // 特色汇款
    By tv_guide_top_2 = By.id("cn.com.spdb.mobilebank.per:id/tv_guide_top_2");
    // 转账汇款/断言-是否位于主页
    By Transfer = By.xpath(
            "//android.widget.GridView[@resource-id='cn.com.spdb.mobilebank.per:id/gv_menu_first_line']/android.widget.RelativeLayout[2]/android.widget.LinearLayout[1]/android.widget.ImageView[1]");
    // 登陆-4位验证码
    By et_login_identify_code = By.id("cn.com.spdb.mobilebank.per:id/et_login_identify_code");
    // 登陆-确认
    By devicebind_btn_bind = By.id("cn.com.spdb.mobilebank.per:id/devicebind_btn_bind");
    // 登陆-动态密码
    By devicebind_edit_dypwd = By.id("cn.com.spdb.mobilebank.per:id/devicebind_edit_dypwd");
    // 登陆-输入6位查询密码
    By et_login_user_pwd = By
            .xpath("//android.widget.EditText[@resource-id='cn.com.spdb.mobilebank.per:id/et_login_user_pwd']");
    // 我的-温馨提示-确认/系统提示-立即更新
    By btn_dialog_positive = By.id("cn.com.spdb.mobilebank.per:id/btn_dialog_positive");
    // 我的-下一步(登陆)
    By btn_login_new_login = By.id("cn.com.spdb.mobilebank.per:id/btn_login_new_login");
    // 我的-请输入手机号开通/登陆
    By et_login_user_phone = By.id("cn.com.spdb.mobilebank.per:id/et_login_user_phone");
    // 左上角"x"
    By iv_login_close = By.id("cn.com.spdb.mobilebank.per:id/iv_login_close");
    // ip设置-更新
    By updatesettingbtn = By.id("cn.com.spdb.mobilebank.per:id/updatesettingbtn");
    // ip设置-97
    By ip_setting_97 = By.id("cn.com.spdb.mobilebank.per:id/ip_setting_97");
    // ip设置-后退
    By title_left_button = By.id("cn.com.spdb.mobilebank.per:id/title_left_button");
    // ip设置-地址
    By ipaddress = By.id("cn.com.spdb.mobilebank.per:id/ipaddress");
    // "i"
    By iv_debug_icon = By.id("cn.com.spdb.mobilebank.per:id/iv_debug_icon");
    // 首页-广告
    By iv_closePage = By.id("cn.com.spdb.mobilebank.per:id/iv_closePage");
    // 首页-我的
    By radio_button5 = By.id("cn.com.spdb.mobilebank.per:id/radio_button5");
    //注册/登录
    By redio_desc = By.xpath("//android.view.View[@content-desc='注册/登录']");
    // 转账汇款图标
    By iv_regular_function_icon = By.id("cn.com.spdb.mobilebank.per:id/iv_regular_function_icon");
    // 首次登陆提示2框
    By bt_second_show = By.id("cn.com.spdb.mobilebank.per:id/bt_second_show");
    // 首次登陆提示1框
    By bt_first_show = By.id("cn.com.spdb.mobilebank.per:id/bt_first_show");
    // 立即体验
    By Immediate = By.xpath("//android.widget.TextView");
    // 首次登陆权限弹窗
    By permission_allow_button = By.id("com.android.packageinstaller:id/permission_allow_button");
    // 浦发银行安全输入
    By Safe_input = By.xpath("//android.widget.TextView[@text='浦发银行安全输入']");
    //SIT2
    By bt_sit2 = By.id("cn.com.spdb.mobilebank.per:id/ip_setting_sit2");
    //生产
    By new_wap = By.id("cn.com.spdb.mobilebank.per:id/ip_setting_new_wap");
    //外网
    By bt_internet = By.id("cn.com.spdb.mobilebank.per:id/btn_setting_internet");
    //点击输入测试页面地址
    By test_page = By.id("cn.com.spdb.mobilebank.per:id/tv_test_page");
    //输入测试地址
    By page_url = By.id("cn.com.spdb.mobilebank.per:id/et_test_page_url");
    //跳转
    By page_go = By.id("cn.com.spdb.mobilebank.per:id/tv_test_go_to_page");
    //版本号
    By version_number = By.id("cn.com.spdb.mobilebank.per:id/et_version_number");
    //更新
    By update= By.id("cn.com.spdb.mobilebank.per:id/title_right_button");
    //卖
    By sale = By.xpath("//android.widget.TextView[@text='卖']");
    //go3
    By go3 = By.xpath("//android.view.View[@text='go3']");
    				   //android.view.View[@content-desc='go3']
                       //android.view.View[@text='go3']
    //go2
    By go2 = By.xpath("//android.view.View[@text='go2']");
    //back
    By back = By.xpath("//android.view.View[@text='back']");
    //backTo
    By backTo = By.xpath("//android.view.View[@text='backTo']");
    //getStackInstances
    By getStackInstances = By.xpath("//android.view.View[@text='getStackInstances']");
    //getStageExtraData
    By getStageExtraData = By.xpath("//android.view.View[@text='getStageExtraData']");
    //getStageParam
    By getStageParam = By.xpath("//android.view.View[@text='getStageParam']");
    //getStageAllParams
    By getStageAllParams = By.xpath("//android.view.View[@text='getStageAllParams']");
    //getStagePath
    By getStagePath = By.xpath("//android.view.View[@text='getStagePath']");
    //replace
    By replace = By.xpath("//android.view.View[@text='replace']");
    //replaceTo
    By replaceTo = By.xpath("//android.view.View[@text='replaceTo']");
    //vConsole
    By vConsole = By.xpath("//android.view.View[@text='vConsole']");
    // go3展开断言
    By icon = By.xpath("//android.view.View[@text='Array [\"servicecomponent-vue-cli-routerA#t...']");
    //go3断言
    By go3_assert = By.xpath("//android.view.View[@text='0: \"servicecomponent-vue-cli-routerA#testa@1\"']");
    //back展开断言
    By back_icon = By.xpath("//android.view.View[@text='Object {a: \"aaa\"}']");
    						 //android.view.View[@content-desc='Object {a: "aaa"}']
    //back断言
    By back_assert = By.xpath("//android.view.View[@text='a: \"aaa\"']");
                               //android.view.View[@content-desc='a: "aaa"']
    //backTo展开断言
    By backTo_icon = By.xpath("//android.view.View[@text='Array [\"servicecomponent-vue-cli-routerA#t...']");
    //backTo断言
    By backTo_assert = By.xpath("//android.view.View[@text='0: \"servicecomponent-vue-cli-routerA#testa@1\"']");
    By backTo_assert2 = By.xpath("//android.view.View[@text='1: \"servicecomponent-vue-cli-routerB#testb@2\"']");
    //getStageExtraData断言
    By getStageExtraData_icon = By.xpath("//android.view.View[@text='Object {extry: \"productlist\"}']");
    By getStageExtraData_assert = By.xpath("//android.view.View[@text='extry: \"productlist\"']");
    By Object_assert = By.xpath("//android.view.View[@text='object']");
     //getStageParam断言
    By getStageParam_assert = By.xpath("//android.view.View[@text='yes']");
    By string_assert = By.xpath("//android.view.View[@text='string']");
    //getStageAllParams
    By getStageAllParams_assert = By.xpath("//android.view.View[@text='Object {a: \"aaa\", b: \"bbb\", x-fullscreen: \"...\']");
    //getStagePath
    By getStagePath_assert = By.xpath("//android.view.View[@text='xxx/servicecomponent-vue-cli-routerA/routerA']");
    //replace
    By replace_assert = By.xpath("//android.view.View[@text='路由 TEST BBBB']");
    //replaceTo
    By replaceTo_icon = By.xpath("//android.view.View[@text='Array [\"servicecomponent-vue-cli-routerA#t...']");
    By replaceTo_assert = By.xpath("//android.view.View[@text='0: \"servicecomponent-vue-cli-routerA#testa@1\"']");
    By replaceTo_assert2 = By.xpath("//android.view.View[@text='1: \"servicecomponent-vue-cli-routerA#testa@4\"']");
    
    /**  任务新增-选中下列框  */
    static Integer time=100;
    static Integer longtime=5000;
    
    By SELECT_CHECKED=By.xpath("/html/body/div[4]/div[2]/div[1]/div[2]/div[2]");
    
    By PEPOLE=By.xpath("/html/body/div[4]/div[2]/div[1]/div[2]/div[1]");
    
    By BANKPEOPLE=By.xpath("/html/body/div[4]/div[2]/div[4]/div[2]/div[1]");
    
    By TASK_NAME=By.xpath("//*[@id='app']/div/div/div[2]/main/div/div/div/div[2]/div/div/div[2]/div/div/div/div/form/div[1]/div/div[1]/div[2]/div/label");
    
    By COMFIRM=By.xpath("//*[@id='app']/div/div/div[2]/main/div/div/div/div[2]/div/div/div[2]/div/div/div/div/form/div[2]/div/button/span[2]");

    By COMFIRM_AGAIN=By.xpath("/html/body/div[4]/div[2]/div/div[3]/button[1]/span[2]/span/span");
}

