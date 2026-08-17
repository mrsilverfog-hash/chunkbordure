# ChunkBorders

Mod client Fabric 1.21.1 — affiche les bordures de chunks avec un rendu custom,
en remplacement du F3+G vanilla (cassé avec Sodium/Iris).

## Touches
| Touche | Effet |
|--------|-------|
| F9     | Afficher / masquer les bordures |

Zone affichée : 3x3 chunks autour du joueur (`CHUNK_RADIUS`). En hauteur, le rendu
est borné au sol sous les pieds et au premier bloc au-dessus de la tête, avec un
plafond de 16 blocs (`VERTICAL_RANGE`).

Rebindables dans Options → Commandes → catégorie « ChunkBorders ».

## Rendu
- Traits **jaunes** : les 4 coins du chunk, sur toute la hauteur du monde
- Traits **cyan** : verticales intermédiaires tous les 4 blocs le long des bords
- Contour **blanc** : au sol, le repère principal
- Contour **jaune discret** : en haut, pour fermer la boîte
- Croix **rouge** posée au sol à chaque intersection de 4 chunks, une branche d'un bloc dans chaque direction

Tout est visible **à travers les murs** (pas de test de profondeur), mais rien ne
dépasse au-dessus du plafond ni en dessous du plancher.

Tout est dessiné en quads (petits pavés), pas en lignes GL — c'est ce qui rend
le mod compatible Sodium/Iris et ce qui permet de régler l'épaisseur.

## Réglages
Dans `ChunkBordersRenderer.java`, en haut du fichier :
- `THICKNESS` (0.06) — épaisseur des traits en blocs
- `VERTICAL_STEP` (4) — espacement des verticales secondaires
- `CHUNK_RADIUS` (1) — 1 = zone 3x3
- `CROSS_ARM` (1.0) — longueur des branches de la croix au sol
- `VERTICAL_RANGE` (16) — hauteur max affichée au-dessus / en dessous du joueur

## Build
Push → onglet Actions → workflow « Build ChunkBorders » → artefact `ChunkBorders`.

Dépôt : https://github.com/mrsilverfog-hash/chunkbordure
