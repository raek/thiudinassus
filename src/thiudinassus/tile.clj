(ns thiudinassus.tile
  "**Tile** contains all functionality for accessing and manipulating
  tiles."
  (:use [thiudinassus.util
         :only (assoc-once map-coll map-keys map-vals update-map)]))

;; ## Tiles
;;
;; A _tile_ has four _edges_ and contains a number of _cities_,
;; _roads_, _fields_ and _monasteries_. The cities, roads, fields and
;; monasteries are also called the _pieces_ of the tile.
;;
;; Each edge has one of three types: _city_, _road_ or _field_. If its
;; type is city, it's connected to one of the cities of the tile.  If
;; its type is field, it's connected to a field. If its type is road,
;; it's connected to one road and two fields.
;;
;; The four edge types of a tile form its _signature_, which is
;; represented as a vector of the types, walking clockwise from
;; north. Example: `[:road :field :road :city]`

;; ## Directions
;;
;; The points of the compass are used to refer to edges and part edges
;; of tiles. For city and field edges, as well as the road part of
;; road edges, _cardinal directions_ are used. For the field parts of
;; road edges, _intermediate directions_ are used.
;;
;; Some abbreviations: **dir** for _direction_, **cdir** for
;; _cardinal direction_ and **idir** for _intermediate direction_.

;; ### Cardinal Directions
;;
;; The cardinal directions are the directions of _north_, _south_,
;; _east_ and _west_. They are represented using the keywords `:n`,
;; `:s`, `:e` and `:w`.

(defn rotate-cdir
  "Rotate the cardinal direction `rot` number of 90° steps clockwise.

   Examples:

       (rotate-cdir +1 :n) => :e
       (rotate-cdir -1 :w) => :s
       (rotate-cdir +2 :e) => :w"
  [rot cdir]
  (let [x ({:n 0, :e 1, :s 2, :w 3} cdir)
        y (mod (+ x rot) 4)]
    ([:n :e :s :w] y)))

;; ### Intermediate Directions
;;
;; "Intermediate directions" means something slightly different here,
;; compared to the normal usage. The used intermediary (sub)directions
;; are: _NNE, ENE, ESE, SSE, SSW, WSW, WNW and NNW_. They are
;; represented as follows: `[:n :e]`, `[:e :n]`, `[:e :s]`, `[:s :e]`,
;; `[:s :w]`, `[:w :s]`, `[:w :n]`, `[:n, :e]`. They could be read as
;; "the eastern part of the north side", "the northern part of the
;; east side", etc.

(defn rotate-idir
  "Rotate the intermediate direction `rot` number of 90° steps
   clockwise.

   Examples:

       (rotate-idir +1 [:n :w]) => [:e :n]
       (rotate-idir -1 [:w :n]) => [:s :w]
       (rotate-idir +2 [:e :s]) => [:w :n]"
  [rot idir]
  (map-coll idir (partial rotate-cdir rot)))

;; ## Edges and Signatures

(defn edge-type-matches?
  "Check whether an edge matches another. Two edge types match if they
   are the same, or if any one of them is nil.

   Examples:

       (edge-type-matches? :road :road) => true
       (edge-type-matches? :road nil)   => true
       (edge-type-matches? nil :road)   => true
       (edge-type-mathces? :city :road) => false"
  [x y]
  (or (nil? x)
      (nil? y)
      (= x y)))

(defn signature-matches?
  "Check whether a signature matches another. Two signatures match if
   all their edge types match element-wise, not concidering rotated
   variants.

   Examples:

       (signature-matches? [:road :field :road :city]
                           [:road :field nil nil])
         => true
       (signature-matches? [:road :field :road :city]
                           [:road :field :road :field])
         => false"
  [x y]
  (every? identity (map edge-type-matches? x y)))

(defn rotate-signature
  "Rotate the signature `rot` number of 90° steps clockwise.

   Examples:

       (rotate-signature 1 [:road :field :road :city])
         => [:city :road :field :road]"
  [rot sig]
  (vec (map (fn [i]
              (sig (mod (- i rot)  4)))
            (range 4))))

(defn- rotated-signature-matches?
  "Rotate the signature `free-sig` `rot` 90° step clockwise and check
   whether it matches `fixed-sig`.

   Examples:

       (rotated-signature-matches?
        1
        [:road :field :road :city]
        [nil :road :field nil nil])
         => true"
  [rot free-sig fixed-sig]
  (signature-matches? (rotate-signature rot free-sig)
                      fixed-sig))

(defn matching-rotations
  "Determine which rotations of the signature `free-sig` match the
   signature `fixed-sig`. The result is given as a sequence of zero to
   four integers; each value in the range 0 to 3 (inclusive).

   Examples:

       (matching-rotations [:road :field :road :city]
                           [nil :road nil nil])
         => (1 3)"
  [free-sig fixed-sig]
  (filter #(rotated-signature-matches? % free-sig fixed-sig)
          (range 4)))

;; ## Tile Operations

(defn tile-signature
  "Returns the signature of the tile."
  [tile]
  (->> [:n :e :s :w]
       (map (:edges tile))
       (map first)
       vec))

(declare rotate-edges rotate-city rotate-road rotate-field)

(defn rotate-tile
  "Rotate the tile `rot` number of 90° steps clockwise."
  [rot tile]
  (update-map tile
              :edges  (partial rotate-edges rot)
              :cities #(map-vals % (partial rotate-city rot))
              :roads  #(map-vals % (partial rotate-road rot))
              :fields #(map-vals % (partial rotate-field rot))))

(defn- rotate-edges [rot edges]
  (map-keys edges (partial rotate-cdir rot)))

(defn- rotate-city [rot city]
  (update-map city
              :edges #(map-coll % (partial rotate-cdir rot))))

(defn- rotate-road [rot road]
  (update-map road
              :edges #(map-coll % (partial rotate-cdir rot))))

(defn- rotate-field [rot field]
  (update-map field
              :edges #(map-coll % (partial rotate-idir rot))))

(def adorn-tile identity)

