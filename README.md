# ChunkBorders

Mod client Fabric 1.21.1 — affiche les bordures de chunks avec un rendu custom,
en remplacement du F3+G vanilla (cassé avec Sodium/Iris).

## Touches
| Touche | Effet |
|--------|-------|
| F9     | Afficher / masquer les bordures |
| F10    | Zone affichée : 1x1 → 3x3 → 5x5 chunks |

Rebindables dans Options → Commandes → catégorie « ChunkBorders ».

## Rendu
- Traits **jaunes** : les 4 coins du chunk, sur toute la hauteur du monde
- Traits **cyan** : verticales intermédiaires tous les 4 blocs le long des bords
- Contours **jaunes horizontaux** : tous les 16 blocs de hauteur, ils relient les verticales

Tout est dessiné en quads (petits pavés), pas en lignes GL — c'est ce qui rend
le mod compatible Sodium/Iris et ce qui permet de régler l'épaisseur.

## Réglages
Dans `ChunkBordersRenderer.java`, en haut du fichier :
- `THICKNESS` (0.06) — épaisseur des traits en blocs
- `VERTICAL_STEP` (4) — espacement des verticales secondaires
- `LEVEL_STEP` (16) — espacement vertical des contours horizontaux

## Build
Push → onglet Actions → workflow « Build ChunkBorders » → artefact `ChunkBorders`.

Dépôt : https://github.com/mrsilverfog-hash/chunkbordure
