Warp
===
Warp is a multiplayer browser game that’s based on GWT, Websockets and libgdx.

Status
===
You can chat, you can fight with the ships, also with NPCs and the demoserver works too.
Fleetbuilding works and save in localStorage.

ATM all code written is throw-away proto code. If more permanent solution will be looked at, language(s) and frameworks might totally change.

Demo: http://warp.ext.vincit.fi (operational only when I start the server)


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


Steps Before First Catalog-tuning, Need to have
===
* Nykyisen buildin autosave, listenereillä tilan muuttuessa
* warp.ext.vincit.fi taas kuntoon
* Katalogin modaaminen netin kautta
    * Katalogin hakeminen http://openkeyval.org/
    * http://jsoneditoronline.org/
    * save / load yksinkertaisilla formeilla + JS warp.ext:iin

Steps Before First Catalog-tuning, Nice to have
===
* Maksiminopeuden asettaminen
    * Numeroista asettaminen
    * Rajoituksen pakottaminen Steeringissä
    * Rajoituksen näyttäminen aluksen lähellä

MVProto
===
* Lentäminen on yhtä nopeaa sivuittain, taaksepäin kuin eteenkin päin.. ainoastaan maksimivoimilla = kiihtymisellä on
  atm merkitystä suunnan suhteen. Pitäisikö maksiminopeutta rajoittaa vastaavasti suunnan perusteella?
* Pitäisikö kyetä kiihdyttämään/jarruttamaan vain aluksen suuntaan?
* Rendaa valituille aluksille
    * DONE Orbit-kursori mikäli orbit on päällä
    * Orbitin ympyränuoli mikäli orbit on päällä
    * DONE Ampumiskohde mikäli sellainen on annettu
    * Direction-nuoli mikäli direction-lento on päällä
    * DONE Go-to-ikoni VAIN valituille aluksille
    * Aktiivisten käskyjen ikonit pitäisi olla jotenkin
        * Keskenään samanlaisia
        * Selkeästi erotettavissa hiiren kursori-ikoneista joita käytetään kun annetaan käskyjä
    * Ikonien kuvien ei pitäisi sisältää sävyä, vaan se voitaisiin asettaa ohjelmallisesti (kaikille samaksi, helppo muuttaminen ja testailu)
        * Paitsi että tuskin mahdollista hiiren kursoreille jotka määritellään CSS:ssä..
* Alusten ja niiden stattien ostaminen kälin kautta
    * Save/load ship
    * Save/load fleet nimillä
    * DONE Delete ship
    * Delete saved fleet
    * DONE Lukumäärän antaminen aluksille
    * DONE Tällä hetkellä aktiivisen laivan nappulan pitäisi erottua muista.. kenties yksinkertaisesti eri värinen?
* Erilaiset alustyypit
    * DONE Koko katalogiin, ja koon välittäminen läpi luontiketjun
    * Alustyyppi ShipStatseihin
    * Eri graffat
    * DONE Eri alustyyppien luominen fleet build windowiin
* Orbit-käskyä annettaessa voisi näyttää numerona rangen ja kiertonopeuden rad/s
* Trailin pitäisi toimia aluksen keskikohdasta
* Isojen alusten hitboxit ovat valtavia, vois pienentää tai jättää kokonaan pois?
* Kääntymisnopeutta voisi myös rajoittaa serverin päässä
* Jotenkin pitäisi rajoittaa ettei kääntymisen max kiihtyminen voi olla suurempi kuin max velocity.. tai korjata steering
* Ampumiskohteen asettaminen helpommaksi:
    * Fire at -mousemoodi.. valkataan kursoria lähin kohde ampumiskohteeksi. Samanlainen moodi päälle - klikkaus-systeemi
* Näytä jokin graffa aluksen tuhoutuessa
* Tällä hetkellä kohti ammusta lentäminen hidastaa ammusta.. ammusten pitää yksinkertaisesti alkaa kulkemaan omaa tasaista vauhtiaan
* Alusten ja niiden stattien ostaminen kälin kautta
    * DONE Katalogin parsiminen -> käyttöliittymä
    * DONE Kokonaishinnan laskeminen ja näyttäminen webbikälissä
    * Osto-UI
        * DONE Take a test-flight -nappi
        * DONE Save/load fleet
        * Save/load ship
        * Nykyisten stattien näyttäminen
        * Yksiköt näkyville
        * DONE Aluksen tyypin valitseminen lisättäessä
        * DONE Lukumäärän antaminen aluksille. Voisi näkyä myös UI:ssa
        * Delete ship
* Alusten statteja voisi muuttaa lennossa kun on testflight -moodissa?
* Go To -komentoon useita eri pisteitä (shift pohjassa)
    * Eri pisteiden visualisointi
    * Ajaminen pisteistä toisiin
    * Eri pisteidelle numerointi (?) tai nuolet pisteiden välillä
* Reunakolmioiden siirtäminen ui-layerille
* Reunakolmion koko voisi riippua siitä, kuinka lähellä kohde on
* Kameran liikuttelu ei saisi tökkiä
* Ei uusia ammuksia aluksiin jotka on jo ehditty tuhoamaan, mutta ovat vielä ruudulla
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
* DONE Erota ammusten piirtäminen damagetekstin näyttämisestä kooditasolla
* DONE Ammuksille matkanopeus, mistä riippuu aika jonka ammus on ruudulla
* DONE Damagetekstin rendaus vasta sen jälkeen kun ammus on osunut
* DONE Damagetekstin rendaus satunnaiseen kohtaan aluksen lähelle, ei aina samaan pinoon
* DONE Chat-ikkuna toimimaan JOIN_CHAT -viesteillä
* DONE Aluksen poistaminen vasta sen jälkeen kun ammus on osunut
* DONE Fleetin Client-side tallettaminen
    * DONE Clientside storage json stringinä
* DONE Pikseliepätarkkuuden selvittäminen
    * Mahdollisimman yksinkertainen esimerkki
    * Toistuuko desktop-versiolla?
* DONE Chat / Fleetbuilding screen yhdistäminen
    * DONE Myös sisäluokkien rikkominen omiin tiedostoihinsa?
* DONE Eri screeneistä aloittaminen toimimaan oikein
* DONE Orbit-etäisyyden muuttamiselle jonkinlainen UI
    * Orbittia annettaessa näytetään nuoli halutulla etäisyydellä, ja hiiren rullalla voi kasvattaa/pienentää etäisyyttä
    * Ehkä alpha 0.5 kaikille aktiivisille käskyille
* DONE Alus pitäisi valita klikatessa ainostaan hiiren default-moodissa.. minkä voisi uudelleennimetä default_selectiksi?
* DONE Määrän ja tyypin näyttäminen buildin valintanapissa
* DONE Selvitä minkä takia fysiikkafixturessa on päddingiä verrattuna graffaan
* DONE Yliampumisen poistaminen steerauksen angular impulsen hakemisesta