/**
 * 需要导入extendscript.js and xlsx.core.min.js
 * @param {} sheet 
 */


function sheet2blob(sheet, sheetName){
    sheetName = sheetName || 'sheet1';
    let workbook = {
        SheetNames: [sheetName],
        Sheets: []
    };
    workbook.Sheets[sheetName] = sheet;
    let wopts = {
        bookType: 'xlsx',
        bookSST: false,
        type: 'binary'
    }
    let wbout = XLSX.write(workbook, wopts);
    let blob = new Blob([s2ab(wbout)], {type:"application/octet-stream"});

    function s2ab(s){
        let buf = new ArrayBuffer(s.length);
        let view = new Uint8Array(buf);
        for (let i = 0; i != s.length ; ++i)
            view[i] = s.charCodeAt(i) & 0xFF;
        return buf;
    }

    return blob;
}

function saveAs(blob, fileName){
    var a = document.createElement('a');
    a.style.display = 'none';
    a.download = fileName;
    a.id = "aexport";

    var lb = document.createElement('label');
    lb.for = "aexport";
    a.appendChild(lb);

    var objectURL = window.URL.createObjectURL(blob);
    a.href = objectURL;

    document.body.appendChild(a);
    lb.click();

    if(_isIE()){
        window.navigator.msSaveOrOpenBlob(blob, fileName);
    }

    document.body.removeChild(a);
    URL.revokeObjectURL(objectURL);
}

function openDownloadDialog(url,saveName){
    if(typeof url == 'object' && url instanceof Blob){
        //创建blob地址
        url = URL.createObjectURL(url);
    }
    let aLink = document.createElement('a');
    aLink.href = url;
    aLink.download = saveName || '';
    let event;
    if (window.MouseEvent)
        event = new MouseEvent('click');
    else{
        event = document.createEvent('MouseEvents');
        event.initMouseEvent('click',true,false,window,0,0,0,0,0,false,false,false,false,0,null);
    }
    aLink.dispatchEvent(event);
}

//处理sheet数据，将'详情'列删除
function deleteCell(sheet){
    //去除sheet中'详情'列
    let cellTitles = [
                        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'
                     ];
    let cellTitle = '';
    for (let i = 0; i < cellTitles.length; i++) {
        let mark = cellTitles[i] + '1';
        if(sheet.hasOwnProperty(mark)){
            if(sheet[mark].hasOwnProperty('v')){
                if(sheet[mark].v.indexOf('详情') >= 0)
                    cellTitle = cellTitles[i];
            }
        }

    }
    if (cellTitle != ''){
        for (let i = 1; i <= sheet["!ref"].slice(4); i++) {
            delete sheet[cellTitle + i];
        }
    }
    return sheet;
}