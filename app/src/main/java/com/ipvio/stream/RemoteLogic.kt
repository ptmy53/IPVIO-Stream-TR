package com.ipvio.stream

/**
 * Phone search (remote control in the browser), pure parts without Android: JSON building blocks and the page for the phone.
 * The TV serves the page (see [PhoneImport]); it searches and starts titles via the API under /api/.
 */
object RemoteLogic {
    /** Text as a JSON string (with quotes). */
    fun q(s: String): String {
        val b = StringBuilder(s.length + 2)
        b.append('"')
        for (c in s) when {
            c == '"' -> b.append("\\\"")
            c == '\\' -> b.append("\\\\")
            c == '\n' -> b.append("\\n")
            c == '\r' -> b.append("\\r")
            c == '\t' -> b.append("\\t")
            c < ' ' -> b.append("\\u").append(String.format("%04x", c.code))
            else -> b.append(c)
        }
        return b.append('"').toString()
    }

    /** A title for the list on the phone: id, title, year, quality, category. */
    fun item(id: String, title: String, year: Int?, quality: String?, group: String): String =
        "{\"id\":" + q(id) + ",\"t\":" + q(title) + ",\"y\":" + (year ?: 0) + ",\"q\":" + q(quality.orEmpty()) + ",\"g\":" + q(group) + "}"

    /** A started title for "Continue watching": id, title, kind (m/s), season, episode, progress in percent. */
    fun resume(id: String, title: String, kind: String, season: Int, ep: Int, pct: Int): String =
        "{\"id\":" + q(id) + ",\"t\":" + q(title) + ",\"k\":" + q(kind) + ",\"s\":$season,\"e\":$ep,\"p\":${pct.coerceIn(0, 100)}}"

    /** A live channel: the [item] JSON plus favorite flag "f" and, if known, the running show "np" with start "ns" and end "ne" (epoch seconds). */
    fun live(item: String, fav: Boolean, nowTitle: String?, start: Long, end: Long): String =
        item.dropLast(1) + ",\"f\":" + fav + (if (nowTitle != null) ",\"np\":" + q(nowTitle) + ",\"ns\":" + start + ",\"ne\":" + end else "") + "}"

    /**
     * A game for the Sports Hub on the phone: key, competition, title (single-player sports), home and away team with score,
     * state (0 not started, 1 live, 2 finished), detail, start (epoch seconds), watchable on a channel, favorite, picked by hand.
     */
    fun game(key: String, league: String, title: String, home: String, away: String, hs: String, aws: String,
             state: Int, detail: String, start: Long, watch: Boolean, fav: Boolean, pinned: Boolean): String =
        "{\"k\":" + q(key) + ",\"lg\":" + q(league) + ",\"ti\":" + q(title) + ",\"h\":" + q(home) + ",\"a\":" + q(away) +
            ",\"hs\":" + q(hs) + ",\"as\":" + q(aws) + ",\"st\":$state,\"d\":" + q(detail) + ",\"t\":$start,\"w\":$watch,\"f\":$fav,\"p\":$pinned}"

    fun arr(items: List<String>): String = items.joinToString(",", "[", "]")

    /** An episode: season, number, title. */
    fun episode(season: Int, num: Int, title: String): String = "{\"s\":$season,\"n\":$num,\"t\":" + q(title) + "}"

    /** Playback state for the display under the buttons. */
    fun state(active: Boolean, title: String, playing: Boolean, posSec: Long, durSec: Long, live: Boolean): String =
        "{\"a\":$active,\"t\":" + q(title) + ",\"pl\":$playing,\"pos\":$posSec,\"dur\":$durSec,\"live\":$live}"

    /** The buttons jump at least this many seconds; larger values are limited. */
    fun clampSkip(sec: Int): Int = sec.coerceIn(-600, 600)

    /** mm:ss or h:mm:ss. */
    fun clock(sec: Long): String {
        val s = sec.coerceAtLeast(0)
        val h = s / 3600; val m = (s % 3600) / 60; val r = s % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, r) else String.format("%d:%02d", m, r)
    }

    private fun tr(li: Int, de: String, en: String): String = when (li) {
        0 -> de
        1 -> en
        else -> I18nCore.translate(de, li).takeIf { it != de } ?: en
    }

    /** Text for an HTML attribute or a JavaScript string in single quotes. */
    private fun js(s: String): String = s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ").replace("<", "\\x3c")

    private fun h(s: String): String = s.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;")

    /** The page for the phone. */
    fun page(li: Int): String {
        fun t(de: String, en: String) = tr(li, de, en)
        val lang = I18nCore.CODES.getOrElse(li) { "en" }
        return """<!doctype html><html lang="$lang"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover"><meta name="theme-color" content="#0b1220">
<title>IPVIO Stream</title>
<style>
*{box-sizing:border-box}
body{margin:0;background:#0b1220;color:#fff;font:16px/1.35 system-ui,sans-serif;padding-bottom:150px}
header{position:sticky;top:0;background:#0b1220;padding:12px 14px 6px;z-index:5}
#q{width:100%;padding:14px 16px;border-radius:14px;border:1px solid #2a3550;background:#16203a;color:#fff;font-size:17px}
#tabs{display:flex;gap:8px;margin-top:10px}
#tabs button{flex:1;padding:10px 4px;border-radius:12px;border:0;background:#16203a;color:#9fb0d0;font-size:15px;font-weight:600}
#tabs button.on{background:#2ee59d;color:#06121f}
main{padding:6px 14px}
.row{display:flex;align-items:center;gap:10px;width:100%;text-align:left;border:0;background:#121b31;color:#fff;border-radius:12px;padding:12px 14px;margin:8px 0;font-size:16px}
.row b{display:block;font-weight:600}
.row small{color:#8ea0c4}
.row .m{flex:1;min-width:0}
.star{font-size:24px;color:#ffc857;padding:0 6px 0 0}
.row .m b,.row .m small{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.bd{background:#2ee59d;color:#06121f;border-radius:6px;padding:2px 7px;font-size:12px;font-weight:700}
.msg{color:#8ea0c4;text-align:center;padding:30px 10px}
.bar{display:flex;gap:8px;align-items:center;margin:8px 0}
.bar select{flex:1;padding:12px;border-radius:12px;border:1px solid #2a3550;background:#16203a;color:#fff;font-size:16px}
.back{border:0;background:#16203a;color:#fff;border-radius:12px;padding:12px 14px;font-size:15px}
#ctl{position:fixed;left:0;right:0;bottom:0;background:#0f1730;border-top:1px solid #243052;padding:8px 10px calc(10px + env(safe-area-inset-bottom))}
#now{font-size:13px;color:#9fb0d0;margin:0 4px 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
#btns{display:grid;grid-template-columns:repeat(4,1fr);gap:6px}
#btns button{padding:12px 2px;border-radius:12px;border:0;background:#1b2750;color:#fff;font-size:14px;font-weight:600}
#btns button.go{background:#2ee59d;color:#06121f}
#toast{position:fixed;left:50%;transform:translateX(-50%);bottom:130px;background:#000c;color:#fff;padding:10px 16px;border-radius:20px;font-size:14px;opacity:0;transition:opacity .2s;pointer-events:none;max-width:90%;text-align:center}
#toast.on{opacity:1}
.top{display:flex;gap:8px}.top #q{flex:1}
#gear{border:0;border-radius:14px;background:#16203a;color:#fff;font-size:22px;width:52px}
.card{background:#121b31;border-radius:12px;padding:12px 14px;margin:8px 0}
.card b{display:block;font-weight:600;overflow:hidden;text-overflow:ellipsis}
.card small{color:#8ea0c4}
.acts{display:flex;gap:6px;flex-wrap:wrap;margin-top:8px}
.sb{border:0;border-radius:10px;padding:9px 11px;background:#1b2750;color:#fff;font-size:14px;font-weight:600}
.sb.on{background:#2ee59d;color:#06121f}
.sec{color:#9fb0d0;font-size:13px;font-weight:700;text-transform:uppercase;margin:18px 2px 4px}
.sub{display:flex;gap:6px;overflow-x:auto;margin:6px 0}
.sub button{flex:none;padding:9px 12px;border-radius:12px;border:0;background:#16203a;color:#9fb0d0;font-size:14px;font-weight:600}
.sub button.on{background:#2ee59d;color:#06121f}
.fld{width:100%;padding:12px;margin:5px 0;border-radius:12px;border:1px solid #2a3550;background:#16203a;color:#fff;font-size:16px}
</style></head><body>
<header><div class="top"><input id="q" type="search" placeholder="${h(t("Filme, Serien, Sender suchen …", "Search movies, series, channels …"))}" autocomplete="off" autocapitalize="off" enterkeyhint="search">
<button id="gear" aria-label="${h(t("Einstellungen", "Settings"))}">&#9881;</button></div>
<div id="tabs"><button data-k="m" class="on">${h(t("Filme", "Movies"))}</button><button data-k="s">${h(t("Serien", "Series"))}</button><button data-k="l">Live TV</button><button data-k="sp">Sport</button></div></header>
<main id="list"></main>
<div id="ctl"><div id="now"></div><div id="btns">
<button data-c="skip" data-v="-30">-30 s</button><button data-c="skip" data-v="-10">-10 s</button><button class="go" data-c="pause">${h(t("Pause / Weiter", "Pause / Play"))}</button>
<button data-c="skip" data-v="10">+10 s</button><button data-c="skip" data-v="30">+30 s</button><button data-c="stop">${h(t("Stopp", "Stop"))}</button>
<button data-c="vol" data-v="-1">${h(t("Leiser", "Vol -"))}</button><button data-c="vol" data-v="1">${h(t("Lauter", "Vol +"))}</button></div></div>
<div id="toast"></div>
<script>
var L={code:'${js(t("Code vom Fernseher:", "Code from the TV:"))}',none:'${js(t("Nichts gefunden.", "Nothing found."))}',hint:'${js(t("Tippe einen Titel ein.", "Type a title."))}',
cats:'${js(t("Kategorien", "Categories"))}',favs:'${js(t("Meine Favoriten", "My favorites"))}',manage:'${js(t("Kategorien ein-/ausblenden", "Show / hide categories"))}',manageHint:'${js(t("Tippen blendet eine Kategorie am Fernseher ein oder aus.", "Tap to show or hide a category on the TV."))}',hidden:'${js(t("ausgeblendet", "hidden"))}',shown:'${js(t("sichtbar", "visible"))}',mylists:'${js(t("Meine Senderlisten", "My channel lists"))}',imp:'${js(t("Liste importieren (Letterboxd, MyAnimeList, Trakt)", "Import list (Letterboxd, MyAnimeList, Trakt)"))}',impDone:'${js(t("Liste wurde an den Fernseher gesendet", "List sent to the TV"))}',back:'${js(t("Zurück", "Back"))}',season:'${js(t("Staffel", "Season"))}',ep:'${js(t("Folge", "Episode"))}',
ok:'${js(t("Läuft auf dem Fernseher.", "Playing on the TV."))}',err:'${js(t("Fehler. Ist der Fernseher noch an?", "Error. Is the TV still on?"))}',
load:'${js(t("Lädt …", "Loading …"))}',
set:'${js(t("Einstellungen", "Settings"))}',pls:'${js(t("Playlists", "Playlists"))}',main:'${js(t("Hauptliste", "Main list"))}',mkMain:'${js(t("Zur Hauptliste machen", "Make main list"))}',
ren:'${js(t("Umbenennen", "Rename"))}',del:'${js(t("Löschen", "Delete"))}',delQ:'${js(t("Diese Playlist wirklich löschen?", "Really delete this playlist?"))}',newName:'${js(t("Neuer Name:", "New name:"))}',
add:'${js(t("Playlist hinzufügen", "Add playlist"))}',m3u:'${js(t("M3U-Link", "M3U link"))}',xt:'${js(t("oder Xtream-Login", "or Xtream login"))}',srv:'${js(t("Server (z. B. http://server.tld:8080)", "Server (e.g. http://server.tld:8080)"))}',
usr:'${js(t("Benutzername", "Username"))}',pw:'${js(t("Passwort", "Password"))}',send:'${js(t("An den Fernseher senden", "Send to TV"))}',sent:'${js(t("Gesendet. Der Fernseher lädt die Playlist.", "Sent. The TV is loading the playlist."))}',
cLive:'${js(t("Live-TV-Kategorien", "Live TV categories"))}',cMov:'${js(t("Film-Kategorien", "Movie categories"))}',cSer:'${js(t("Serien-Kategorien", "Series categories"))}',genres:'${js(t("Genres", "Genres"))}',
catHint:'${js(t("Die Pfeile verschieben eine Kategorie, „Ausblenden“ blendet sie aus, der Stern macht sie zum Favoriten (nur Live TV). Gilt sofort am Fernseher.", "The arrows move a category, “Hide” hides it, the star makes it a favorite (Live TV only). Applies on the TV right away."))}',
up:'${js(t("Hoch", "Up"))}',down:'${js(t("Runter", "Down"))}',top:'${js(t("Anfang", "Top"))}',bottom:'${js(t("Ende", "Bottom"))}',hide:'${js(t("Ausblenden", "Hide"))}',showC:'${js(t("Einblenden", "Show"))}',
adult:'${js(t("Jugendschutz", "Parental control"))}',adultOn:'${js(t("Inhalte für Erwachsene sind ausgeblendet.", "Adult content is hidden."))}',adultOff:'${js(t("Inhalte für Erwachsene werden angezeigt.", "Adult content is shown."))}',
adultHide:'${js(t("Ausblenden", "Hide"))}',adultShow:'${js(t("Anzeigen (PIN nötig)", "Show (PIN needed)"))}',adultShow0:'${js(t("Anzeigen", "Show"))}',pinQ:'${js(t("PIN vom Fernseher:", "PIN from the TV:"))}',pinBad:'${js(t("Falsche PIN.", "Wrong PIN."))}',
lang:'${js(t("Sprache der App", "App language"))}',auto:'${js(t("Automatisch (Gerät)", "Automatic (device)"))}',done:'${js(t("Gespeichert.", "Saved."))}',
today:'${js(t("Heute", "Today"))}',sfav:'${js(t("Favoriten", "Favorites"))}',mine:'${js(t("Meine Liste", "My list"))}',teams:'${js(t("Lieblingsteams", "Favorite teams"))}',
watch:'${js(t("Ansehen", "Watch"))}',pin:'${js(t("Merken", "Keep"))}',pinned:'${js(t("Gemerkt", "Kept"))}',live:'LIVE',
noGames:'${js(t("Keine Spiele gefunden.", "No games found."))}',noFav:'${js(t("Noch keine Lieblingsteams. Wähle sie unter „Lieblingsteams“.", "No favorite teams yet. Choose them under “Favorite teams”."))}',
noMine:'${js(t("Noch keine Spiele gemerkt. Tippe bei einem Spiel auf „Merken“; es erscheint dann auch am Fernseher im Mini-Ergebnis.", "No games kept yet. Tap “Keep” on a game; it then also shows in the mini score on the TV."))}',
sOff:'${js(t("Live-Ergebnisse sind am Fernseher ausgeschaltet (Sports Hub).", "Live scores are switched off on the TV (Sports Hub)."))}',noCh:'${js(t("Kein passender Sender gefunden.", "No matching channel found."))}',
league:'${js(t("Liga wählen", "Choose a league"))}',cont:'${js(t("Weiterschauen", "Continue watching"))}',idle:'${js(t("Es läuft nichts.", "Nothing is playing."))}',wait:'${js(t("Die Playlist wird noch geladen.", "The playlist is still loading."))}'};
var code='';try{code=new URLSearchParams(location.search).get('code')||sessionStorage.getItem('ipvio_code')||''}catch(e){}
var kind='m',data={m:[],s:[],l:[]},cat=null,tm=null,seq=0;
var qEl=document.getElementById('q'),list=document.getElementById('list'),toastEl=document.getElementById('toast');
function toast(s){toastEl.textContent=s;toastEl.className='on';clearTimeout(toast.t);toast.t=setTimeout(function(){toastEl.className=''},2200)}
function api(p,o){var u='/api/'+p+'?code='+encodeURIComponent(code);for(var k in o||{})u+='&'+k+'='+encodeURIComponent(o[k]);
 return fetch(u).then(function(r){if(r.status==403||r.status==429){var c=r.status==403?prompt(L.code):null;if(c){code=c;try{sessionStorage.setItem('ipvio_code',c)}catch(e){}}throw 0}
  if(!r.ok)throw 1;return r.json()})}
function el(t,c,x){var e=document.createElement(t);if(c)e.className=c;if(x!=null)e.textContent=x;return e}
function msg(s){list.innerHTML='';list.appendChild(el('div','msg',s))}
function row(title,sub,badge,fn){var b=el('button','row'),m=el('div','m');m.appendChild(el('b',null,title));if(sub)m.appendChild(el('small',null,sub));b.appendChild(m);
 if(badge)b.appendChild(el('span','bd',badge));b.onclick=fn;return b}
function play(id,s,n){var o={id:id};if(s!=null){o.s=s;o.e=n}api('play',o).then(function(){toast(L.ok)}).catch(function(e){if(e!==0)toast(L.err)})}
function hm(s){var d=new Date(s*1000);return (d.getHours()<10?'0':'')+d.getHours()+':'+(d.getMinutes()<10?'0':'')+d.getMinutes()}
function liveRow(it,sub0){var sub=it.np?it.np+'  '+hm(it.ns)+' - '+hm(it.ne):(sub0||'');
 var b=row(it.t,sub,it.q,function(){play(it.id)});
 var st=el('span','star',it.f?'\u2605':'\u2606');st.onclick=function(ev){ev.stopPropagation();api('fav',{id:it.id}).then(function(d){it.f=d.f;st.textContent=d.f?'\u2605':'\u2606'}).catch(function(e){if(e!==0)toast(L.err)})};
 b.insertBefore(st,b.firstChild);return b}
function items(a){list.innerHTML='';if(!a.length){msg(L.none);return}
 a.forEach(function(it){var sub=[it.y?it.y:'',it.g].filter(Boolean).join(' · ');
  if(kind=='l')list.appendChild(liveRow(it,sub));
  else list.appendChild(row(it.t,sub,it.q,function(){if(kind=='s')eps(it);else play(it.id)}))})}
function eps(it){msg(L.load);api('eps',{id:it.id}).then(function(a){list.innerHTML='';
 var bar=el('div','bar'),bk=el('button','back','< '+L.back);bk.onclick=show;bar.appendChild(bk);
 var seasons=[];a.forEach(function(e){if(seasons.indexOf(e.s)<0)seasons.push(e.s)});seasons.sort(function(x,y){return x-y});
 var sel=el('select');seasons.forEach(function(s){var o=el('option',null,L.season+' '+s);o.value=s;sel.appendChild(o)});bar.appendChild(sel);list.appendChild(bar);
 list.appendChild(el('div','msg',it.t));var box=el('div');list.appendChild(box);
 function fill(){box.innerHTML='';a.filter(function(e){return e.s==sel.value}).forEach(function(e){box.appendChild(row(e.n+'. '+e.t,null,null,function(){play(it.id,e.s,e.n)}))})}
 sel.onchange=fill;fill()}).catch(function(e){if(e!==0)msg(L.err)})}
function chList(call,o,backFn,backLabel,title){msg(L.load);api(call,o).then(function(a){list.innerHTML='';var bk=el('button','back','< '+backLabel);bk.onclick=backFn;list.appendChild(bk);
 if(title)list.appendChild(el('div','msg',title));if(!a.length){list.appendChild(el('div','msg',L.none));return}
 a.forEach(function(it){list.appendChild(liveRow(it))})}).catch(function(e){if(e!==0)msg(L.err)})}
function cats(){msg(L.load);api('cats').then(function(a){list.innerHTML='';
 var fb=row(L.favs,null,null,function(){chList('favs',{},cats,L.cats,L.favs)});list.appendChild(fb);
 list.appendChild(el('div','msg',L.mylists));
 api('lists').then(function(ls){ls.forEach(function(l){list.insertBefore(row(l.g,l.n+'',null,function(){chList('list',{i:l.i},cats,L.cats,l.g)}),mark)})}).catch(function(){});
 var mark=el('div','msg',L.cats);list.appendChild(mark);if(!a.length){return}
 a.forEach(function(c){list.appendChild(row(c.g,c.n+'',null,function(){chans(c.g)}))})}).catch(function(e){if(e!==0)msg(L.err)})}
function importList(){var f=document.createElement('input');f.type='file';f.onchange=function(){var x=f.files[0];if(!x)return;msg(L.load);
 fetch('/listimport?name='+encodeURIComponent(x.name)+'&code='+encodeURIComponent(code),{method:'POST',body:x}).then(function(r){if(!r.ok)throw 1;toast(L.impDone);settings()}).catch(function(){toast(L.err);settings()})};f.click()}
function chans(g){chList('channels',{g:g},cats,L.cats,null)}
function manage(){msg(L.load);api('groups').then(function(a){list.innerHTML='';var bk=el('button','back','< '+L.back);bk.onclick=cats;list.appendChild(bk);
 list.appendChild(el('div','msg',L.manageHint));
 a.forEach(function(c){var b=row(c.g,c.n+'',c.h?L.hidden:L.shown,null);
  b.onclick=function(){c.h=!c.h;api('hide',{g:c.g,h:c.h?1:0}).then(function(){var bd=b.querySelector('.bd');bd.textContent=c.h?L.hidden:L.shown;b.style.opacity=c.h?0.5:1}).catch(function(e){c.h=!c.h;if(e!==0)toast(L.err)})};
  if(c.h)b.style.opacity=0.5;list.appendChild(b)})}).catch(function(e){if(e!==0)msg(L.err)})}
function sb(label,fn,on){var b=el('button','sb'+(on?' on':''),label);b.onclick=function(ev){ev.stopPropagation();fn(b)};return b}
function card(title,sub,badge){var c=el('div','card');var b=el('b',null,title);if(badge){var x=el('span','bd',badge);x.style.marginLeft='8px';b.appendChild(x)}c.appendChild(b);if(sub)c.appendChild(el('small',null,sub));return c}
function head(title,backFn){list.innerHTML='';var bk=el('button','back','< '+L.back);bk.onclick=backFn;list.appendChild(bk);if(title)list.appendChild(el('div','sec',title))}
function fail(e){if(e!==0)toast(L.err)}
function settings(){head(L.set,show);
 [[L.pls,plView],[L.cLive,function(){catView('live')}],[L.cMov,function(){catView('movie')}],[L.cSer,function(){catView('series')}],
  [L.imp,importList],[L.teams,function(){teamsView(false)}],[L.adult,adultView],[L.lang,langView]].forEach(function(x){list.appendChild(row(x[0],null,null,x[1]))})}
function plView(){msg(L.load);api('pls').then(function(a){head(L.pls,settings);
 a.forEach(function(p){var c=card(p.n,p.h,p.a?L.main:null),acts=el('div','acts');
  if(!p.a)acts.appendChild(sb(L.mkMain,function(){api('plmain',{i:p.i}).then(function(){toast(L.done);setTimeout(plView,800)}).catch(fail)}));
  acts.appendChild(sb(L.ren,function(){var n=prompt(L.newName,p.n);if(n&&n.trim())api('plren',{i:p.i,n:n.trim()}).then(function(){setTimeout(plView,300)}).catch(fail)}));
  acts.appendChild(sb(L.del,function(){if(confirm(L.delQ+'\n'+p.n))api('pldel',{i:p.i}).then(function(){toast(L.done);setTimeout(plView,800)}).catch(fail)}));
  c.appendChild(acts);list.appendChild(c)});
 list.appendChild(el('div','sec',L.add));
 var f=el('div','card');f.appendChild(el('small',null,L.m3u));var u=el('input','fld');u.placeholder='http://…/playlist.m3u';f.appendChild(u);
 f.appendChild(el('small',null,L.xt));var hs=el('input','fld');hs.placeholder=L.srv;var us=el('input','fld');us.placeholder=L.usr;us.autocapitalize='off';var pw=el('input','fld');pw.placeholder=L.pw;pw.autocapitalize='off';
 f.appendChild(hs);f.appendChild(us);f.appendChild(pw);
 f.appendChild(sb(L.send,function(){var b='code='+encodeURIComponent(code);
  if(u.value.trim())b+='&url='+encodeURIComponent(u.value.trim());else b+='&host='+encodeURIComponent(hs.value.trim())+'&user='+encodeURIComponent(us.value.trim())+'&pass='+encodeURIComponent(pw.value.trim());
  fetch('/send',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:b}).then(function(r){if(!r.ok)throw 1;toast(L.sent);u.value='';pw.value=''}).catch(function(){toast(L.err)})},true));
 list.appendChild(f)}).catch(function(e){if(e!==0)msg(L.err)})}
function catView(a,keep){var y=keep?window.scrollY:0;if(!keep)msg(L.load);api('vcats',{a:a}).then(function(d){head(a=='live'?L.cLive:a=='movie'?L.cMov:L.cSer,settings);
 list.appendChild(el('div','msg',L.catHint));
 var gens=d.c.filter(function(x){return x.gen}),cs=d.c.filter(function(x){return !x.gen});
 if(gens.length){list.appendChild(el('div','sec',L.genres));gens.forEach(function(x){var c=card(x.l,null,x.h?L.hidden:null),acts=el('div','acts');
  acts.appendChild(sb(x.h?L.showC:L.hide,function(){api('vhide',{a:a,g:x.g,h:x.h?0:1}).then(function(){catView(a,1)}).catch(fail)}));c.appendChild(acts);if(x.h)c.style.opacity=0.55;list.appendChild(c)});
  list.appendChild(el('div','sec',L.cats))}
 cs.forEach(function(x){var c=card(x.g,x.n+'',x.h?L.hidden:null),acts=el('div','acts');
  function mv(dir){api('vmove',{a:a,g:x.g,d:dir}).then(function(){catView(a,1)}).catch(fail)}
  acts.appendChild(sb('↑ '+L.up,function(){mv('up')}));acts.appendChild(sb('↓ '+L.down,function(){mv('down')}));
  acts.appendChild(sb('⇡ '+L.top,function(){mv('top')}));acts.appendChild(sb('⇣ '+L.bottom,function(){mv('bottom')}));
  if(a=='live')acts.appendChild(sb(x.f?'★':'☆',function(){api('vfav',{g:x.g}).then(function(){setTimeout(function(){catView(a,1)},200)}).catch(fail)},x.f));
  acts.appendChild(sb(x.h?L.showC:L.hide,function(){api('vhide',{a:a,g:x.g,h:x.h?0:1}).then(function(){setTimeout(function(){catView(a,1)},200)}).catch(fail)}));
  c.appendChild(acts);if(x.h)c.style.opacity=0.55;list.appendChild(c)});
 if(keep)window.scrollTo(0,y)}).catch(function(e){if(e!==0)msg(L.err)})}
function adultView(){msg(L.load);api('adult').then(function(d){head(L.adult,settings);var c=card(d.hide?L.adultOn:L.adultOff,null,null),acts=el('div','acts');
 if(d.hide)acts.appendChild(sb(d.pin?L.adultShow:L.adultShow0,function(){var p=d.pin?prompt(L.pinQ):'';if(d.pin&&!p)return;
  api('adultset',{v:0,pin:p||''}).then(function(r){if(r.ok===false){toast(L.pinBad);return}toast(L.done);setTimeout(adultView,300)}).catch(fail)}));
 else acts.appendChild(sb(L.adultHide,function(){api('adultset',{v:1}).then(function(){toast(L.done);setTimeout(adultView,300)}).catch(fail)},true));
 c.appendChild(acts);list.appendChild(c)}).catch(function(e){if(e!==0)msg(L.err)})}
function langView(){msg(L.load);api('langs').then(function(d){head(L.lang,settings);
 [{c:'',n:L.auto}].concat(d.l).forEach(function(x){list.appendChild(row(x.n,null,x.c==d.c?'✓':null,function(){api('setlang',{c:x.c}).then(function(){toast(L.done);setTimeout(langView,300)}).catch(fail)}))})}).catch(function(e){if(e!==0)msg(L.err)})}
var sub='today',sdata={},stime={};
function day(s){var d=new Date(s*1000),n=new Date();if(d.toDateString()==n.toDateString())return hm(s);return d.getDate()+'.'+(d.getMonth()+1)+'. '+hm(s)}
function subBar(){var b=el('div','sub');[['today',L.today],['fav',L.sfav],['mine',L.mine],['teams',L.teams]].forEach(function(x){var t=el('button',x[0]==sub?'on':null,x[1]);
 t.onclick=function(){sub=x[0];sportView()};b.appendChild(t)});return b}
function gameCard(g){var title=g.ti?g.ti:(g.h+(g.st>0?'  '+g.hs+' : '+g.as+'  ':'  vs  ')+g.a);
 var c=card(title,g.lg+' · '+(g.st==1?L.live+' '+g.d:g.st==2?g.d:day(g.t)),g.st==1?L.live:null),acts=el('div','acts');
 if(g.w&&g.st<2)acts.appendChild(sb('▶ '+L.watch,function(){api('swatch',{k:g.k}).then(function(r){toast(r.ok?L.ok+' '+r.ch:L.noCh)}).catch(fail)},true));
 acts.appendChild(sb(g.p?'✓ '+L.pinned:L.pin,function(b){api('spin',{k:g.k}).then(function(r){g.p=r.p;b.textContent=r.p?'✓ '+L.pinned:L.pin;stime.mine=0}).catch(fail)},g.p));
 c.appendChild(acts);return c}
function sportView(){if(sub=='teams'){teamsView(true);return}
 var draw=function(d){list.innerHTML='';list.appendChild(subBar());
  if(d.off){list.appendChild(el('div','msg',L.sOff));return}
  var q=qEl.value.trim().toLowerCase(),a=d.g.filter(function(g){return !q||(g.h+' '+g.a+' '+g.ti+' '+g.lg).toLowerCase().indexOf(q)>=0});
  if(!a.length){list.appendChild(el('div','msg',sub=='fav'&&d.nofav?L.noFav:sub=='mine'?L.noMine:L.noGames));return}
  a.forEach(function(g){list.appendChild(gameCard(g))})};
 if(sdata[sub]&&Date.now()-(stime[sub]||0)<30000){draw(sdata[sub]);return}
 list.innerHTML='';list.appendChild(subBar());list.appendChild(el('div','msg',L.load));var me=sub;
 api('sport',{v:sub}).then(function(d){sdata[me]=d;stime[me]=Date.now();if(sub==me&&kind=='sp')draw(d)}).catch(function(e){if(e!==0)msg(L.err)})}
var tLeague='';
function teamsView(inSport){msg(L.load);api('sleagues').then(function(ls){list.innerHTML='';
 if(inSport)list.appendChild(subBar());else{head(L.teams,settings)}
 if(!ls.length){list.appendChild(el('div','msg',L.noGames));return}
 if(!tLeague||!ls.some(function(l){return l.p==tLeague}))tLeague=ls[0].p;
 var sel=el('select','fld');ls.forEach(function(l){var o=el('option',null,l.n);o.value=l.p;if(l.p==tLeague)o.selected=true;sel.appendChild(o)});list.appendChild(sel);
 var box=el('div');list.appendChild(box);
 function fill(){box.innerHTML='';box.appendChild(el('div','msg',L.load));api('steams',{p:tLeague}).then(function(ts){box.innerHTML='';if(!ts.length){box.appendChild(el('div','msg',L.noGames));return}
  ts.forEach(function(t){var r=row(t.n,null,null,null),st=el('span','star',t.f?'★':'☆');r.insertBefore(st,r.firstChild);
   r.onclick=function(){api('stfav',{p:tLeague,id:t.id}).then(function(x){t.f=x.f;st.textContent=x.f?'★':'☆';stime.fav=0}).catch(fail)};box.appendChild(r)})}).catch(function(e){if(e!==0)box.innerHTML=''})}
 sel.onchange=function(){tLeague=sel.value;fill()};fill()}).catch(function(e){if(e!==0)msg(L.err)})}
document.getElementById('gear').onclick=settings;
var rdata=null,rt=0;
function resumeView(){var go=function(){list.innerHTML='';var a=(rdata||[]).filter(function(x){return x.k==kind});
 if(!a.length){msg(L.hint);return}
 list.appendChild(el('div','msg',L.cont));
 a.forEach(function(x){var sub=(x.k=='s'&&x.s?L.season+' '+x.s+' · '+L.ep+' '+x.e:'')+(x.p>0?(x.k=='s'&&x.s?' · ':'')+x.p+' %':'');
  list.appendChild(row(x.t,sub,null,function(){if(x.k=='s'&&x.s)play(x.id,x.s,x.e);else play(x.id)}))})};
 if(rdata&&Date.now()-rt<30000){go();return}
 api('resume').then(function(d){rdata=d;rt=Date.now();if(!qEl.value.trim())go()}).catch(function(e){if(e!==0)msg(L.hint)})}
function show(){var q=qEl.value.trim();
 if(kind=='sp'){sportView();return}
 if(!q){if(kind=='l')cats();else resumeView();return}
 items(data[kind])}
function search(){var q=qEl.value.trim(),me=++seq;if(!q||kind=='sp'){show();return}
 api('search',{q:q}).then(function(d){if(me!=seq)return;if(d.loading){msg(L.wait);return}data=d;
  var bs=document.querySelectorAll('#tabs button');bs[0].textContent=d.nm?'${js(t("Filme", "Movies"))} '+d.nm:'${js(t("Filme", "Movies"))}';
  bs[1].textContent=d.ns?'${js(t("Serien", "Series"))} '+d.ns:'${js(t("Serien", "Series"))}';bs[2].textContent=d.nl?'Live TV '+d.nl:'Live TV';show()}).catch(function(e){if(e!==0)msg(L.err)})}
qEl.oninput=function(){clearTimeout(tm);tm=setTimeout(search,300)};
Array.prototype.forEach.call(document.querySelectorAll('#tabs button'),function(b){b.onclick=function(){kind=b.getAttribute('data-k');
 Array.prototype.forEach.call(document.querySelectorAll('#tabs button'),function(x){x.className=x==b?'on':''});show()}});
Array.prototype.forEach.call(document.querySelectorAll('#btns button'),function(b){b.onclick=function(){
 api('ctl',{c:b.getAttribute('data-c'),v:b.getAttribute('data-v')||''}).then(function(d){if(d&&d.idle)toast(L.idle)}).catch(function(e){if(e!==0)toast(L.err)})}});
function clock(s){s=Math.max(0,s|0);var h=(s/3600)|0,m=((s%3600)/60)|0,r=s%60;return(h?h+':'+(m<10?'0':''):'')+m+':'+(r<10?'0':'')+r}
function poll(){api('state').then(function(d){var n=document.getElementById('now');
 n.textContent=d.a?(d.t?d.t+'  ':'')+(d.live?'LIVE':clock(d.pos)+(d.dur>0?' / '+clock(d.dur):''))+(d.pl?'':'  ||'):L.idle}).catch(function(){})}
setInterval(poll,3000);poll();show();
</script></body></html>"""
    }
}
