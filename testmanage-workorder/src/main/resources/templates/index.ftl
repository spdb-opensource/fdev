<!DOCTYPE HTML>
<html>
<head>
    <style>
            html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre, a, abbr, acronym, address, big, cite, code, del, dfn, em, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup, tt, var, b, u, i, dl, dt, dd, ol, nav ul, nav li, fieldset, form, label, legend, table, caption, tbody, tfoot, thead, tr, th, td, article, aside, canvas, details, embed, figure, figcaption, footer, header, hgroup, menu, nav, output, ruby, section, summary, time, mark, audio, video {
                margin: 0;
                padding: 0;
                border: 0;
                font-size: 100%;
                font: inherit;
                vertical-align: baseline;
            }

            article, aside, details, figcaption, figure, footer, header, hgroup, menu, nav, section {
                display: block;
            }

            ol, ul {
                list-style: none;
                margin: 0;
                padding: 0;
            }

            blockquote, q {
                quotes: none;
            }

            blockquote:before, blockquote:after, q:before, q:after {
                content: '';
                content: none;
            }

            table {
                border-collapse: collapse;
                border-spacing: 0;
            }

    		td {
    			border: black 1px solid;
                line-height: 30px;
                height: 30px;
                padding-left: 3px;
    		}

            /* start editing from here */
            a {
                text-decoration: none;
            }

            .txt-rt {
                text-align: right;
            }

            /* text align right */
            .txt-lt {
                text-align: left;
            }

            /* text align left */
            .txt-center {
                text-align: center;
            }

            /* text align center */
            .float-rt {
                float: right;
            }

            /* float right */
            .float-lt {
                float: left;
            }

            /* float left */
            .clear {
                clear: both;
            }

            /* clear float */
            .pos-relative {
                position: relative;
            }

            /* Position Relative */
            .pos-absolute {
                position: absolute;
            }

            /* Position Absolute */
            .vertical-base {
                vertical-align: baseline;
            }

            /* vertical align baseline */
            .vertical-top {
                vertical-align: top;
            }

            /* vertical align top */
            .underline {
                padding-bottom: 5px;
                border-bottom: 1px solid #eee;
                margin: 0 0 20px 0;
            }

            /* Add 5px bottom padding and a underline */
            nav.vertical ul li {
                display: block;
            }

            /* vertical menu */
            nav.horizontal ul li {
                display: inline-block;
            }

            /* horizontal menu */
            img {
                max-width: 100%;
            }

            /*end reset*/
            /*--login start here--*/
            body {
                font-size: 100%;
                background: white;
                font-family: 'Roboto Slab', serif;
            }

            a {
                text-decoration: none;
            }

            a:hover {
                transition: 0.5s all;
                -webkit-transition: 0.5s all;
                -moz-transition: 0.5s all;
                -o-transition: 0.5s all;
            }

            /*--header start here--*/
            .newsletter {
                <#--background: url(xxx/${path}/banner.png) no-repeat;-->
                background-color: white;
                background-size: cover;
                min-height: 600px;
            }

            .newsletter-main {
                width: 90%;
                margin: 0 auto;
                padding: 0.5em 0em;
                text-align: center;
            }

            .stamp img {
                width: 20%;
                margin-top: 20px;
                -webkit-transform: rotate(15deg);
                -moz-transform: rotate(15deg);
                transform: rotate(15deg);
            }

            .stamp {
                text-align: right;
            }

            .newsletter-main h1 {
                font-size: 2em;
                color: #000;
                font-weight: 300;
            }

            .newsletter-main h2 {
                font-size: 2em;
                color: #000;
                margin: 0em 0em 0.5em 0em;
            }

            .newsletter-main p {
                font-size: 1.1em;
                color: #000;
                line-height: 1.5em;
                width: 80%;
                margin: 0.5em auto 0.5em;
            }

            .newsletter-main pre {
                text-align: left;
            }

            a.signup {
                font-size: 1.2em;
                color: #4882ce;
                font-weight: 600;
                display: block;
                margin: 0em 0em 1em 0em;
            }

            .newsletter-main input[type="text"] {
                font-size: 1em;
                color: #000;
                padding: 0.8em 1em;
                border: 3px solid #4882ce;
                border-radius: 5px;
                display: inline-block;
                width: 80%;
                outline: none;
                text-align: center;
                margin: 0em auto 1em;
                font-family: 'Roboto Slab', serif;
            }

            .newsletter-main input[type="submit"] {
                font-size: 1.2em;
                color: #fff;
                padding: 0.7em 2.5em;
                outline: none;
                border: none;
                border-radius: 5px;
                background: #00ab00;
                display: inline-block;
                margin-bottom: 1.1em;
                cursor: pointer;
                font-family: 'Roboto Slab', serif;
            }

            .newsletter-main input[type="submit"]:hover {
                background: #4882ce;
            }

            .tlg {
                margin: 1em 0em 1em 0em;
            }

            .tlg-img {
                float: left;
                width: 20%;
            }

            .tlg-text {
                float: right;
                width: 80%;
                text-align: left;
            }

            .tlg-text h3 {
                font-size: 1.6em;
                color: #166EC4;
            }

            .tlg-text h4 {
                font-size: 1em;
                color: #000;
            }

            .tlg-img img {
                width: 60%;
            }

            .clear {
                clear: both;
            }

            /*---copyrights--*/
            .copy-right {
                margin: 3em 0em 2em 0em;
            }

            .copy-right p {
                text-align: center;
                font-size: 1em;
                color: #fff;
                line-height: 1.5em;
                font-family: 'Quicksand', sans-serif;
            }

            .copy-right p a {
                color: #fff;
            }

            .copy-right p a:hover {
                color: #fff;
                text-decoration: underline;
                transition: 0.5s all;
                -webkit-transition: 0.5s all;
                -moz-transition: 0.5s all;
                -o-transition: 0.5s all;
            }


                .tablecss{
                    width:70%;
                    border-color:#EBEEF5;
            }
                 .tr-height{
                    height:25px;
            }
                 .item1{
                    width:20%;
            }
                .td-width{
                    width:500;
                    background:#B4F096;
                    font-weight: bold;
            }
            .td-width2{
                    width:200;
                    background:#B4F096;
                    font-weight: bold;
            }
        </style>
    <!-- Custom Theme files -->
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
</head>
<body>
<div class="newsletter">
    <div class="newsletter-main">
        <h2 style="font-family: STKaiti">${subject}</h2>
        <div class="design">
            <img src="xxx/fuser/design.png"/>
        </div>
        <br><br>
        <#include "/${template_name}.ftl"/>
        <div class="design">
            <img src="xxx/fuser/design.png"/>
        </div>
        <div class="clear"></div>
    </div>
    <div class="copy-right" style="margin-left: 5%;margin-right: 5%">
        <p style="color: black;">提示：请不要对此邮件直接回复，系统看不懂您的回信^_^。如果您有建议或意见，请回复
            <a href="mailto:xxx" style="text-decoration:none; color:blue;">xxx</a></p>
    </div>
</div>

</body>
</html>