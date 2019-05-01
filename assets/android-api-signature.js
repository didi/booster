/**
 * Downloading Android API signature list from browser
 *
 * @author johnsonlee
 */
window.location = 'data:application/octet-stream,' + encodeURIComponent(Array.prototype.map.call(document.querySelectorAll("div[data-version-added] pre.api-signature"), signature =>
    signature.innerHTML
            .replace(/\n/g, '')
            .replace(/\s{2,}/g, ' ')
            .replace(/<a\s+href="https:\/\/developer\.android\.com\/reference\/(.+?)\.html">.+?<\/a>/g, '$1')
            .replace(/&lt;.+?&gt;/g, '')
            .replace(/\s*(public|protected|private|final|abstract|static)\s*/g, '')
).filter(signature => signature.indexOf('(') > 0).map(signature => {
    var sp = signature.indexOf(' ');
    var lp = signature.indexOf('(', sp);
    var name = signature.substring(sp, lp).trim();
    var desc = signature.substr(lp)
            .replace(/([^\s,\(\)]+) ([^\s,\(\)]+)/g, '$1')
            .replace(/\./g, '$')
            .replace(/([a-zA-Z]+(\/[a-zA-Z\$]+)?)\[\]/g, '[$1')
            .replace(/int/g, 'I')
            .replace(/void/g, 'V')
            .replace(/boolean/g, 'Z')
            .replace(/byte/g, 'B')
            .replace(/char/g, 'C')
            .replace(/long/g, 'J')
            .replace(/float/g, 'F')
            .replace(/double/g, 'D')
            .replace(/([a-zA-Z]+(\/[a-zA-Z\$]+)+)/g, 'L$1;')
            .replace(/\s*,\s*/g, '');
    var rt = signature.substring(0, sp)
            .replace(/int/g, 'I')
            .replace(/void/g, 'V')
            .replace(/boolean/g, 'Z')
            .replace(/byte/g, 'B')
            .replace(/char/g, 'C')
            .replace(/long/g, 'J')
            .replace(/float/g, 'F')
            .replace(/double/g, 'D')
            .replace(/([a-zA-Z]+(\/[a-zA-Z\$]+)+)/g, 'L$1;');
    return name + desc + rt;
}).filter(signature => !/^\(\)/.test(signature)).map(signature => `"${signature}"`).join(',\n'))
