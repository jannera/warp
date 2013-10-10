Warp
===
Warp is a multiplayer browser game that’s based on GWT, Websockets and libgdx.

Status
===
You can chat, you can move the ships, and the demoserver works too. Maybe you can do some shooting soon too ;)

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
Super Dev Mode is nice! (and not too hard to set up)
Making GWT artefacts with Intellij Idea is occultism (and maybe not the best idea)

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
DONE voimien rajoittaminen aluksen suunnan perusteella
- Kameraan liikuttelu (ja zoomaus?)
- Jokin tapa "nähdä" nykyisen ruudun ulkopuolelle: Jonkinlaisen kartan näyttäminen? Väritettyjä kolmioita ruudun reunaan?
- Laivojen omistaminen
- Eri tyyppien laivojen värittäminen
- Valitun aluksen korostaminen
- Ainoastaan omien laivojen käskyttäminen client-puolelle
- Oikea määrä pikseleitä tappeluruudulle. resoluution suhde ei saa muuttua kun vaihtaa ikkunan kokoa
- Ampumisen UI:
  - klikkaa alusta, klikkaa kohdetta
  - näytetään tgt-marker kohteen päällä
- Alukset törmäämään toisiinsa server-päässä.. ei ramming speediä kuitenkaan?
- Ampumissysteemi
  - aseiden statit: sykli, lämä
  - ampumiskomento clientilta
  - loput hienoudet: tracking, optimal, falloff.. koko even systeemi ja nopanheitto
  - ammusten näyttäminen clintissä
  - lämän näyttäminen clientissä
- healthin näyttäminen clientissä
- Oikea määrä pikseleitä chatti-ikkunalle
- Chatissa olevien tyyppien nimien listaaminen
- Pelin aloittaminen chatista:
  - Haasteen antaminen: /fight <player> <player> <player> ...
  - Haasteen hyväksyminen: /accept <player>
  - Pelin aloittaminen
- Kyky pyörittää useita pelejä samaan aikaan
- maksimivoimien antaminen Serveriltä aluksen luonnin yhteydessä
- arrival liikkumiseen
- maksimi-impulsien pituudet pitäisi kertoa ShipSteering.STEP_LENGTH:in pituudella, jolloin aluksen maksimivoimat ilmaisisivat käytetyn maksimivoiman (keskiarvon) sekunnissa
- aseiden rangen näyttäminen ympyrällä
- usean laivan kerralla
  - valitseminen
  - navigoiminen
  - hyökkäyskäskyn antaminen
  - asioiden visualisoiminen