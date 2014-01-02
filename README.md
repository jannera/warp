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
* Lentäminen on yhtä nopeaa sivuittain, taaksepäin kuin eteenkin päin.. ainoastaan maksimivoimilla = kiihtymisellä on
  atm merkitystä suunnan suhteen. Pitäisikö maksiminopeutta rajoittaa vastaavasti suunnan perusteella?
* Alus pitäisi valita klikatessa ainostaan hiiren default-moodissa.. minkä voisi uudelleennimetä default_selectiksi?
* Erota ammusten piirtäminen damagetekstin näyttämisestä kooditasolla
* Ammuksille matkanopeus, mistä riippuu aika jonka ammus on ruudulla
* Damagetekstin rendaus vasta sen jälkeen kun ammus on osunut
* Damagetekstin rendaus satunnaiseen kohtaan aluksen lähelle, ei aina samaan pinoon
* Aluksen poistaminen vasta sen jälkeen kun ammus on osunut
* Ei ammuksia aluksiin jotka on jo ehditty tuhoamaan, mutta ovat vielä ruudulla
* Näytä jokin graffa aluksen tuhoutuessa
* Rendaa valituille aluksille
    * Orbit-kursori ja ympyränuoli mikäli orbit on päällä
    * Ampumiskohde mikäli sellainen on annettu
    * Direction-nuoli mikäli direction-lento on päällä
    * DONE Go-to-ikoni mikäli Go To on päällä
    * Aktiivisten käskyjen ikonit pitäisi olla jotenkin
        * Keskenään samanlaisia
        * Selkeästi erotettavissa hiiren kursori-ikoneista joita käytetään kun annetaan käskyjä
* Ampumiskohteen asettaminen helpommaksi:
    * Fire at -mousemoodi.. valkataan kursoria lähin kohde ampumiskohteeksi. Samanlainen moodi päälle - klikkaus-systeemi
* Maksiminopeuden asettaminen
    * Numeroista asettaminen
    * Rajoituksen pakottaminen Steeringissä
    * Rajoituksen näyttäminen aluksen lähellä
* Alusten ja niiden stattien ostaminen webbikälin kautta
    * Hintalistan parsiminen
    * Kokonaishinnan laskeminen ja näyttäminen webbikälissä
    * Ostonappulat
    * Nykyisten stattien näyttäminen webbikälissä
* Go To -komentoon useita eri pisteitä (shift pohjassa)
    * Eri pisteiden visualisointi
    * Ajaminen pisteistä toisiin
    * Eri pisteidelle numerointi (?) tai nuolet pisteiden välillä
* Orbit-etäisyyden muuttamiselle jonkinlainen UI
    * Orbittia annettaessa näytetään nuoli halutulla etäisyydellä, ja hiiren rullalla voi kasvattaa/pienentää etäisyyttä
* Reunakolmioiden siirtäminen ui-layerille
* Reunakolmion koko voisi riippua siitä, kuinka lähellä kohde on
* Kameran liikuttelu ei saisi tökkiä
* Ruudulle voisi heittää fadeout-viestejä UI:sta, esim kun vaihdetaan optimaalin piirtotilaa
* Healthbar ja teksti pitäisi olla aina aluksen alla, riippumatta aluksen rotaatiosta
* Muuta resoluutiota kun selaimen koko muuttuu (muuttamatta kuvasuhdetta)
* multiselectin pitäisi alkaa vaikka alottaisi laivan päältä
    * vasta sekä ShipClickListenerin että StageListenerin päästessä touchUp:iin, pitäisi selvittää mitä tehdään
* Serverin pitäisi poistaa laivat siinä vaiheessa mikäli
    * DONE yhteys käyttäjään katkeaa
    * tai tulee virhe tiedonsiirrossa
* vain valitun kohteen targetpositioiden näyttäminen? tosin näistä ehkä hankkiudutaan eroon eniveis, joten antaa odottaa?
* Chatissa olevien tyyppien nimien listaaminen
* Pelin aloittaminen chatista:
    * Haasteen antaminen: /fight <player> <player> <player> ...
    * Haasteen hyväksyminen: /accept <player>
    * Pelin aloittaminen
* Kyky pyörittää useita pelejä samaan aikaan
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
* DONE Yksittäisen laivan helpompi valitseminen, sekä oman laivan valinta että ampumiskohteen valinta
* DONE NPC
    * DONE Optimaalille liikkuminen
    * DONE Kohteiden vaihtaminen randomisti
* DONE NPC-fleetin kasaaminen pojoperustaisesti muutaman aluksen mahdollisuuksista
* DONE Parempi fontti ruudulle
* DONE BUGI: kuolleiden omien laivojen optimaalit eivät pitäisi näkyä enää.. taivat jäädä valituiksi vaikka tuhoutuvat
* DONE FPS näkymään alakulmaan
* DONE Vastustajan optimaalien näkeminen
* DONE Suorituskyky: muutamalla kymmenellä laivalla FPS tipahtaa. Tutki miksi
* DONE Laivojen törmäykset pitäisi olla vähemmän kimmoisia
* DONE Fysiikkamoottorin fixturet pitäisi saada abt vastaamaan graffoja
* DONE Optimaalien näyttäminen syklisesti: omat, vastustajan, kaikki, ei mitään
* DONE Jokaiselle ClientShipille tekstikenttä, mihin asetettu teksti piirretään aluksen alla
* DONE kullekin laivalle näyttää numerona sen lähellä
    * nopeuden
    * osumismahdollisuuden / odotetun lämän valittu(j)a laivaa kohti
* DONE CTRL+A sykliseksi: omat, vastustajan, kaikki, ei mitään
* DONE Suuntanavigointi
* DONE Hiiren kursorin vaihtaminen kuviksi CSS-tyyleillä
* DONE Kameralle aluksen seuraaminen
* DONE Suuntanavigointi loppumaan painalluksesta (kaikki muutkin päättyvät)
* DONE Valintaboksin pitää alkaa toimimaan ruutukoordinaatistossa (muuten kameran liike aiheuttaa ongelmia)
* DONE Navigointipisteiden siirtäminen ui-layerille
* DONE Kaiken alle taustagridi.. visualisoi alusten liikkeen paremmin
* DONE Ampumissysteemi
    * DONE aseiden statit: sykli, lämä
    * DONE ampumiskomento clientilta
    * DONE loput hienoudet: tracking, optimal, falloff.. koko even systeemi ja nopanheitto
    * DONE ammusten näyttäminen clientissä
    * DONE lämän näyttäminen clientissä
* DONE Navigointikäskyjä:
    * DONE Orbit, CW/CCW
    * DONE Go to (nykyinen pistenavigointi aktivoituna näppiksen kautta)
* DONE Oikea määrä pikseleitä tappeluruudulle. resoluution suhde ei saa muuttua kun vaihtaa ikkunan kokoa
* DONE Oikea määrä pikseleitä chatti-ikkunalle
