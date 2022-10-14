<template>
  <div class="trade">
    <h1 id="toc_0">交易扫描规范</h1>

    <p>
      本规范适用于纳入开发协作平台Fdev的微服务应用，如其属于web接入层应用（xxx-web-xxx），则须遵守该规范，否则可能对交易信息扫描的准确性造成影响。
    </p>

    <ul>
      <li>
        <h5 id="toc_1">应用命名规范</h5>

        <p>
          请遵守规范
          <a
            href="http://xxx/ebank/doc/blob/master/%E5%BE%AE%E6%9C%8D%E5%8A%A1%E5%BC%80%E5%8F%91%E8%A7%84%E8%8C%83/%E5%BE%AE%E6%9C%8D%E5%8A%A1%E9%A1%B9%E7%9B%AE%E8%A7%84%E8%8C%83.md"
            target="_blank"
            >http://xxx/ebank/doc/blob/master/%E5%BE%AE%E6%9C%8D%E5%8A%A1%E5%BC%80%E5%8F%91%E8%A7%84%E8%8C%83/%E5%BE%AE%E6%9C%8D%E5%8A%A1%E9%A1%B9%E7%9B%AE%E8%A7%84%E8%8C%83.md</a
          >
        </p>
      </li>
      <li>
        <h5 id="toc_2">交易文件目录</h5>

        <ol>
          <li>
            <p>存放交易文件路径：</p>

            <div>
              <pre><code class="language-java">src\main\resources\config\spdb\trans\</code></pre>
            </div>

            <p>
              注：<strong>项目主目录名及子目录名应与项目名称保持一致。</strong>
            </p>

            <p>扫描文件名 ：trs<em>xxx.xml , 或者trans</em>xxx.xml</p>

            <p>
              示例：src\main\resources\config\spdb\trans\fund\trans_fund.xml
            </p>
          </li>
        </ol>
      </li>
      <li>
        <h5 id="toc_3">交易信息扫描规则</h5>

        <p>扫描信息：</p>

        <table>
          <thead>
            <tr>
              <th>扫描项</th>
              <th>说明</th>
            </tr>
          </thead>

          <tbody>
            <tr>
              <td>交易码</td>
              <td>
                <code
                  >&lt;transaction
                  id=&quot;xxx&quot;&gt;...&lt;/transaction&gt;</code
                >
              </td>
            </tr>
            <tr>
              <td>交易描述</td>
              <td><code>&lt;description&gt;xxx&lt;/description&gt;</code></td>
            </tr>
            <tr>
              <td>交易渠道</td>
              <td>
                <code
                  >&lt;channel type=&quot;xxx&quot;&gt;...&lt;/channel&gt;</code
                >,依据交易渠道扫描说明(见下文)
              </td>
            </tr>
            <tr>
              <td>是否登录</td>
              <td>依据交易登录扫描规则(见下文)</td>
            </tr>
            <tr>
              <td>是否记流水</td>
              <td>依据交易流水扫描规则(见下文)</td>
            </tr>
            <tr>
              <td>图片验证码</td>
              <td>依据交易图片验证码扫描规则(见下文)</td>
            </tr>
          </tbody>
        </table>
      </li>
      <li>
        <p><strong>交易渠道扫描说明</strong></p>

        <p>支持扫描三种渠道的交易：<em>http、html、client</em></p>
      </li>
      <li>
        <p><strong>登录扫描规则</strong></p>

        <ol>
          <li>
            <p>根据<em>transaction</em>配置获取到使用的模板， 示例：</p>

            <div>
              <pre><code class="language-markup">&lt;transaction id=&quot;RealGoldOrder&quot; template=&quot; NotLoginTemplate&quot;&gt;</code></pre>
            </div>
          </li>
          <li>
            <p>
              读取文件<em>pe-template.xml</em>，路径：<code
                >src\main\resources\config\core\pe-template.xml</code
              >
            </p>

            <div>
              <pre><code class="language-markup">&lt;template id=&quot;NotLoginTemplate&quot; class=&quot;xxx&quot; chain=&quot;chainForRoleControl&quot; /&gt;</code></pre>
            </div>
          </li>
          <li>
            <p>
              根据<em>chain</em>名称读取<em>pe-chain.xml</em>,路径：<code
                >src\main\resources\config\core\pe-chain.xml</code
              >
            </p>

            <div>
              <pre><code class="language-markup">&lt;chain id=&quot;chainForRoleControl&quot;&gt;
&lt;commands&gt;
    &lt;ref&gt;roleControlCommand&lt;/ref&gt; --- 如果含有此command则为登录交易
    &lt;ref&gt;signatureCommand&lt;/ref&gt;
   &lt;/commands&gt;
&lt;/chain&gt;</code></pre>
            </div>
          </li>
        </ol>

        <ul>
          <li>
            <p><strong>交易流水扫描规则</strong></p>
          </li>
        </ul>

        <ol>
          <li>
            <p>根据transaction配置获取</p>

            <div>
              <pre><code class="language-markup">&lt;setting&gt;
&lt;param name=&quot;WriteLog&quot;&gt;yes&lt;/param&gt;    --- Yes：记流水，其他为不记流水
&lt;/setting&gt;

或者
&lt;setting&gt;
 &lt;param name=&quot;WriteJnl&quot;&gt;false&lt;/param&gt;    --- true-表示记流水，没有配置或false-表示不记流水
&lt;/setting&gt;</code></pre>
            </div>

            <p>如果transaction有配置，以此配置为准。</p>
          </li>
          <li>
            <p>
              如果transaction
              无配置，则查询该transaction使用的模板配置，使用其配置
            </p>

            <div>
              <pre><code class="language-markup">&lt;template id=&quot;commonTemplate&quot; class=&quot;XX&quot; chain=&quot;chainForRoleControl&quot;&gt;
&lt;setting&gt;
    &lt;param name=&quot;WriteLog&quot;&gt;no&lt;/param&gt; 
    &lt;param name=&quot;DBTransaction&quot;&gt;no&lt;/param&gt;
&lt;/setting&gt;
&lt;/template&gt;</code></pre>
            </div>
          </li>
        </ol>

        <ul>
          <li><strong><p>图片验证码扫描规则</p></strong>

            <div>
              <pre><code class="language-markup">&lt;setting&gt; 
      &lt;param name=&quot;VerCodeType&quot;&gt;Jy&lt;/param&gt;  
  &lt;/setting&gt;</code></pre>
            </div>
            <p>图片验证码类型： VerCode-图片验证码， Jy-极验， JyAndVerCode-降级处理</p>
          </li>
        </ul>

        

        
      </li>
      <li>
        <p><strong>交易文件目录与规范</strong></p>

        <p>如某交易含有渠道client，则代表其向客户端提供接口，则做扫描处理。</p>

        <div>
          <pre><code class="language-markup">&lt;channel type=&quot;client&quot; &gt;
&lt;param name=&quot;success&quot;&gt;xml,RealGoldOrder &lt;/param&gt;
&lt;/channel&gt;</code></pre>
        </div>

        <p><strong>xmlView须与所属交易名同名</strong></p>
      </li>
    </ul>

    <p>接口文档文件名：<code>r交易名+“.xml”</code>请求文档名</p>

    <p><code>s交易名+“.xml”</code>响应文档名</p>

    <p>示例：<em>rRealGoldOrder.xml</em></p>

    <p>路径：<code>src\main\resources\packets\mclient\</code></p>

    <p>请求/响应头文件路径：</p>

    <p><code>src\main\resources\packets\mclient\XmlRequestPacket.xml</code></p>

    <p>
      <code>src\main\resources\packets\mclient\XmlResponsePacket.xml</code>
    </p>

    <p>接口报文映射文件</p>

    <ul>
      <li><p>属性扫描：全部采集</p></li>
      <li>
        <p>文件示例：为了展示效果，字段条数稍作删减。</p>

        <p><em>rRealGoldOrder.xml</em></p>

        <div>
          <pre><code class="language-markup">&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
&lt;xmlSegment&gt;
        &lt;xmlElementWhithChild name=&quot;data&quot;&gt;
            &lt;fixStringElement name=&quot;PrdId &quot; trim=&quot;true&quot; /&gt;
        &lt;/xmlElementWhithChild&gt;
&lt;/xmlSegment&gt;</code></pre>
        </div>

        <p><em>sRealGoldOrder.xml</em></p>

        <div>
          <pre><code class="language-markup">&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
&lt;xmlSegment&gt;
    &lt;xmlElementWhithChild name=&quot;data&quot;&gt;
        &lt;fixAmountElement  name=&quot;PrdDiscount&quot; decimal=&quot;2&quot; /&gt;
        &lt;fixStringElement  name=&quot;PrdGoldCom&quot; trim=&quot;true&quot; /&gt;
        &lt;xmlElementWhithChild name=&quot;List&quot; &gt; 
            &lt;loopXmlElement name=&quot;Items&quot; alias=&quot;List&quot; &gt;
                &lt;fixStringElement name=&quot;OrderBuyNum&quot; trim=&quot;true&quot; /&gt;
            &lt;/loopXmlElement&gt;
        &lt;/xmlElementWhithChild&gt;
    &lt;/xmlElementWhithChild&gt;
&lt;/xmlSegment&gt;</code></pre>
        </div>
      </li>
      <li><p>备注：废弃、下线交易接口，请及时更新对应该映射文件。</p></li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'Schema'
};
</script>

<style type="text/css" scoped>
* {
  width: 100%;
}
.trade {
  font-family: Helvetica, arial, sans-serif;
  font-size: 14px;
  line-height: 1.6;
  padding-bottom: 10px;
  width: 100%;
  background-color: white;
  padding: 30px;
  padding-top: 10px;
}

.trade > *:first-child {
  margin-top: 0 !important;
}
.trade > *:last-child {
  margin-bottom: 0 !important;
}

a {
  color: #4183c4;
}
a.absent {
  color: #cc0000;
}
a.anchor {
  display: block;
  padding-left: 30px;
  margin-left: -30px;
  cursor: pointer;
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
}
p {
  word-wrap: wrap;
  word-break: break-all;
}
pre {
  width: 100%;
}
h1,
h2,
h3,
h4,
h5,
h6 {
  margin: 20px 0 10px;
  padding: 0;
  font-weight: bold;
  -webkit-font-smoothing: antialiased;
  cursor: text;
  position: relative;
}

h1:hover a.anchor,
h2:hover a.anchor,
h3:hover a.anchor,
h4:hover a.anchor,
h5:hover a.anchor,
h6:hover a.anchor {
  background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA09pVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoMTMuMCAyMDEyMDMwNS5tLjQxNSAyMDEyLzAzLzA1OjIxOjAwOjAwKSAgKE1hY2ludG9zaCkiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6OUM2NjlDQjI4ODBGMTFFMTg1ODlEODNERDJBRjUwQTQiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6OUM2NjlDQjM4ODBGMTFFMTg1ODlEODNERDJBRjUwQTQiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo5QzY2OUNCMDg4MEYxMUUxODU4OUQ4M0REMkFGNTBBNCIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo5QzY2OUNCMTg4MEYxMUUxODU4OUQ4M0REMkFGNTBBNCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PsQhXeAAAABfSURBVHjaYvz//z8DJYCRUgMYQAbAMBQIAvEqkBQWXI6sHqwHiwG70TTBxGaiWwjCTGgOUgJiF1J8wMRAIUA34B4Q76HUBelAfJYSA0CuMIEaRP8wGIkGMA54bgQIMACAmkXJi0hKJQAAAABJRU5ErkJggg==)
    no-repeat 10px center;
  text-decoration: none;
}

h1 tt,
h1 code {
  font-size: inherit;
}

h2 tt,
h2 code {
  font-size: inherit;
}

h3 tt,
h3 code {
  font-size: inherit;
}

h4 tt,
h4 code {
  font-size: inherit;
}

h5 tt,
h5 code {
  font-size: inherit;
}

h6 tt,
h6 code {
  font-size: inherit;
}

h1 {
  font-size: 31.5px;
  color: black;
}

h2 {
  font-size: 24px;
  border-bottom: 1px solid #cccccc;
  color: black;
}

h3 {
  font-size: 18px;
}

h4 {
  font-size: 16px;
}

h5 {
  font-size: 14px;
}

h6 {
  color: #777777;
  font-size: 14px;
}

p,
blockquote,
ul,
ol,
dl,
li,
table,
pre {
  margin: 15px 0;
}

hr {
  background: transparent
    url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAYAAAAECAYAAACtBE5DAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNSBNYWNpbnRvc2giIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6OENDRjNBN0E2NTZBMTFFMEI3QjRBODM4NzJDMjlGNDgiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6OENDRjNBN0I2NTZBMTFFMEI3QjRBODM4NzJDMjlGNDgiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo4Q0NGM0E3ODY1NkExMUUwQjdCNEE4Mzg3MkMyOUY0OCIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo4Q0NGM0E3OTY1NkExMUUwQjdCNEE4Mzg3MkMyOUY0OCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PqqezsUAAAAfSURBVHjaYmRABcYwBiM2QSA4y4hNEKYDQxAEAAIMAHNGAzhkPOlYAAAAAElFTkSuQmCC)
    repeat-x 0 0;
  border: 0 none;
  color: #cccccc;
  height: 4px;
  padding: 0;
}

.trade > h2:first-child {
  margin-top: 0;
  padding-top: 0;
}
.trade > h1:first-child {
  margin-top: 0;
  padding-top: 0;
}
.trade > h1:first-child + h2 {
  margin-top: 0;
  padding-top: 0;
}
.trade > h3:first-child,
.trade > h4:first-child,
.trade > h5:first-child,
.trade > h6:first-child {
  margin-top: 0;
  padding-top: 0;
}

a:first-child h1,
a:first-child h2,
a:first-child h3,
a:first-child h4,
a:first-child h5,
a:first-child h6 {
  margin-top: 0;
  padding-top: 0;
}

h1 p,
h2 p,
h3 p,
h4 p,
h5 p,
h6 p {
  margin-top: 0;
}

li p.first {
  display: inline-block;
}
li {
  margin: 0;
}
ul,
ol {
  padding-left: 30px;
}

ul :first-child,
ol :first-child {
  margin-top: 0;
}

dl {
  padding: 0;
}
dl dt {
  font-size: 14px;
  font-weight: bold;
  font-style: italic;
  padding: 0;
  margin: 15px 0 5px;
}
dl dt:first-child {
  padding: 0;
}
dl dt > :first-child {
  margin-top: 0;
}
dl dt > :last-child {
  margin-bottom: 0;
}
dl dd {
  margin: 0 0 15px;
  padding: 0 15px;
}
dl dd > :first-child {
  margin-top: 0;
}
dl dd > :last-child {
  margin-bottom: 0;
}

blockquote {
  border-left: 4px solid #dddddd;
  padding: 0 15px;
  color: #777777;
}
blockquote > :first-child {
  margin-top: 0;
}
blockquote > :last-child {
  margin-bottom: 0;
}

table {
  padding: 0;
  border-collapse: collapse;
}
table tr {
  border-top: 1px solid #cccccc;
  background-color: white;
  margin: 0;
  padding: 0;
}
table tr:nth-child(2n) {
  background-color: #f8f8f8;
}
table tr th {
  font-weight: bold;
  border: 1px solid #cccccc;
  margin: 0;
  padding: 6px 13px;
}
table tr td {
  border: 1px solid #cccccc;
  margin: 0;
  padding: 6px 13px;
}
table tr th :first-child,
table tr td :first-child {
  margin-top: 0;
}
table tr th :last-child,
table tr td :last-child {
  margin-bottom: 0;
}

img {
  max-width: 100%;
}

span.frame {
  display: block;
  overflow: hidden;
}
span.frame > span {
  border: 1px solid #dddddd;
  display: block;
  float: left;
  overflow: hidden;
  margin: 13px 0 0;
  padding: 7px;
  width: auto;
}
span.frame span img {
  display: block;
  float: left;
}
span.frame span span {
  clear: both;
  color: #333333;
  display: block;
  padding: 5px 0 0;
}
span.align-center {
  display: block;
  overflow: hidden;
  clear: both;
}
span.align-center > span {
  display: block;
  overflow: hidden;
  margin: 13px auto 0;
  text-align: center;
}
span.align-center span img {
  margin: 0 auto;
  text-align: center;
}
span.align-right {
  display: block;
  overflow: hidden;
  clear: both;
}
span.align-right > span {
  display: block;
  overflow: hidden;
  margin: 13px 0 0;
  text-align: right;
}
span.align-right span img {
  margin: 0;
  text-align: right;
}
span.float-left {
  display: block;
  margin-right: 13px;
  overflow: hidden;
  float: left;
}
span.float-left span {
  margin: 13px 0 0;
}
span.float-right {
  display: block;
  margin-left: 13px;
  overflow: hidden;
  float: right;
}
span.float-right > span {
  display: block;
  overflow: hidden;
  margin: 13px auto 0;
  text-align: right;
}

code,
tt {
  margin: 0 2px;
  padding: 0 5px;
  white-space: nowrap;
  border: 1px solid #eaeaea;
  background-color: #f8f8f8;
  border-radius: 3px;
}

pre code {
  margin: 0;
  padding: 0;
  white-space: pre;
  border: none;
  background: transparent;
}

.highlight pre {
  background-color: #f8f8f8;
  border: 1px solid #cccccc;
  font-size: 13px;
  line-height: 19px;
  overflow: auto;
  padding: 6px 10px;
  border-radius: 3px;
}

pre {
  background-color: #f8f8f8;
  border: 1px solid #cccccc;
  font-size: 13px;
  line-height: 19px;
  overflow: auto;
  padding: 6px 10px;
  border-radius: 3px;
}
pre code,
pre tt {
  background-color: transparent;
  border: none;
}

sup {
  font-size: 0.83em;
  vertical-align: super;
  line-height: 0;
}

kbd {
  display: inline-block;
  padding: 3px 5px;
  font-size: 11px;
  line-height: 10px;
  color: #555;
  vertical-align: middle;
  background-color: #fcfcfc;
  border: solid 1px #ccc;
  border-bottom-color: #bbb;
  border-radius: 3px;
  box-shadow: inset 0 -1px 0 #bbb;
}
</style>
