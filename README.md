Warp
===
Warp is a multiplayer browser game that’s based on GWT, Websockets and libgdx.

Status
===
You can chat, you can move the ships and the demoserver works too.
Each player has his own fleet, that is read from JSON.
JSON can be modified via web interface.
You can shoot, and the damage model works.

ATM all code written is throw-away proto code. If more permanent solution will be looked at, language(s) and frameworks might totally change.

Demo: http://warp.ext.vincit.fi (operational only when I start the server)

Steps before Hackfest
===
* DONE Screen where you can move a spaceship around: UI, messaging, server side handling
* Integrate Artemis (Entity System framework) to (at least) server
* Chat-screen to list online users
* DONE Demo running to ext.vincit.fi

Steps
===
* DONE GWT based Chat Server + Client
* DONE Better messaging system
* Change Websocket library to one that really supports byte[] messaging
* Ship/fleet building window

Stuff Learned
===
* Super Dev Mode is nice! (and not too hard to set up)
* Making GWT artefacts with Intellij Idea is occultism (and maybe not the best idea)

Idea
===
Multiplayer browser game, where you build a fleet of spaceships via point-system, and then lead your fleet in a fight against other players in 2d game, in 1v1, multiple players vs multiple players or death match.

End Goal
===
Learn to use GWT + Websockets, most likely pick up other useful libraries or tools along the way

Future Ideas
===
* Authentication of players
* Persisting data on server
* Tournaments
* Other game modes, for example king of the hill
* Continuous game mode, where every player has fixed amount of warps per day. Every “warp” connects to another player 1v1. Based on how well the fight went, each player gets points. Points are used to build the fleet.
* Later on, maybe transfer all authentication + fleet management (+ possibly also chat) to a web application, maybe Spring + Backbone/Marionette or Meteor. Then combat screen somehow accesses this data stored in DB, maybe via REST API

MVProto
===
* Yksittäisen laivan helpompi valitseminen, sekä oman laivan valinta että ampumiskohteen valinta
* Ampumissysteemi
    * DONE aseiden statit: sykli, lämä
    * DONE ampumiskomento clientilta
    * DONE loput hienoudet: tracking, optimal, falloff.. koko even systeemi ja nopanheitto
    * ammusten näyttäminen clientissä
    * DONE lämän näyttäminen clientissä
* Kameran liikuttelu ei saisi tökkiä
* Fysiikkamoottorin fixturet pitäisi saada abt vastaamaan graffoja
* Parempi fontti ruudulle
* näppiskomennot, mistä voi kullekin laivalle näyttää numerona sen lähellä
    * nopeuden
    * osumismahdollisuuden / odotetun lämän valittu(j)a laivaa kohti
* Oikea määrä pikseleitä tappeluruudulle. resoluution suhde ei saa muuttua kun vaihtaa ikkunan kokoa
* Oikea määrä pikseleitä chatti-ikkunalle
* multiselectin pitäisi alkaa vaikka alottaisi laivan päältä
    * vasta sekä ShipClickListenerin että StageListenerin päästessä touchUp:iin, pitäisi selvittää mitä tehdään

* Serverin pitäisi poistaa laivat siinä vaiheessa mikäli
    * DONE yhteys käyttäjään katkeaa
    * tai tulee virhe tiedonsiirrossa
* vain valitun kohteen targetpositioiden näyttäminen? tosin näistä ehkä hankkiudutaan eroon eniveis, joten antaa odottaa?
* Alusten ja niiden stattien ostaminen webbikälin kautta
    * Hintalistan parsiminen
    * Kokonaishinnan laskeminen ja näyttäminen webbikälissä
    * Ostonappulat
    * Nykyisten stattien näyttäminen webbikälissä
* Chatissa olevien tyyppien nimien listaaminen
* Pelin aloittaminen chatista:
    * Haasteen antaminen: /fight <player> <player> <player> ...
    * Haasteen hyväksyminen: /accept <player>
    * Pelin aloittaminen
* Kyky pyörittää useita pelejä samaan aikaan
* BUGI: joskus lähestyessä pistettä, laiva töksähtää yllättäen kummalliseen suuntaan suht nopeasti
* arrival liikkumiseen
* Korvaa ArrayListit libgdx:n Array:illa

MVProto DONE
===
* DONE voimien rajoittaminen aluksen suunnan perusteella
* DONE Jokin tapa "nähdä" nykyisen ruudun ulkopuolelle: Jonkinlaisen kartan näyttäminen? Väritettyjä kolmioita ruudun reunaan?
* DONE Laivojen omistaminen
* DONE Eri tyyppien laivojen värittäminen
* DONE Valitun aluksen korostaminen
* DONE Ampumisen UI:
    * klikkaa alusta, klikkaa kohdetta
    * näytetään tgt-marker kohteen päällä
* DONE Ainoastaan omien laivojen käskyttäminen client-puolelle
* DONE Kun pelaaja poistetaan pelistä, pitää ilmoittaa peliin jääville pelaajille alusten tuhoutumisesta
* DONE Serveriin väri-indeksien kierrättäminen
* DONE Maksiminopeuden rajoittaminen (serveriin?)
* DONE Suurempi maksimikääntönopeus (lisää FPS:ää?)
* DONE healthin näyttäminen clientissä
* DONE maksimivoimien antaminen Serveriltä aluksen luonnin yhteydessä
* DONE maksimi-impulsien pituudet pitäisi kertoa ShipSteering.STEP_LENGTH:in pituudella, jolloin aluksen maksimivoimat ilmaisisivat käytetyn maksimivoiman (keskiarvon) sekunnissa
* DONE Bugi: kun on antanut laivalle ampumiskohteen, ei saa enää valittua liikkumiskohdetta
    * Ehkä voisi katsoa koko event-systeemiä fiksummaksi.. miksi canceloinnit eivät toimi?
* DONE aseiden rangen näyttäminen ympyrällä
* DONE Kameran zoomi
    * Toimimaan yleensäkin
    * Toimimaan rullasta
* DONE ShipStats omaan luokkaansa
* DONE Harjoitusvastustaja tasoa berserk
* Usean laivan kerralla
    * DONE valitseminen
    * DONE navigoiminen
    * DONE hyökkäyskäskyn antaminen
    * DONE asioiden visualisoiminen
