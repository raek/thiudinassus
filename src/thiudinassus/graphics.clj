(ns thiudinassus.graphics
  (:require [thiudinassus.tile :as tile])
  (:use [thiudinassus.util :only (map-vals update-map)]
        [robert.hooke :only (add-hook)])
  (:import (java.awt Color BasicStroke Dimension Graphics2D)
           (java.awt.geom Path2D$Double)
           (javax.swing JPanel JFrame)))

;; # The Tango color palette

(def tango
  {:butter      [0xfce94f 0xedd400 0xc4a000]
   :orange      [0xfcaf3e 0xf57900 0xcc5c00]
   :chocolate   [0xe9b96e 0xc17d11 0x8f5902]
   :chameleon   [0x8ae234 0x73d216 0x4e9a06]
   :sky-blue    [0x729fcf 0x3465a4 0x204a87]
   :plum        [0xad7fa8 0x75507b 0x5c3566]
   :scarlet-red [0xef2929 0xcc0000 0xa40000]
   :aluminium   [0xcccccc 0xd3d7cf 0xbabdb6
                 0x888a85 0x555753 0x2e3436]})

(defn- tango-color
  "Looks up the shade of the color in the tango palette."
  [color shade]
  (Color. (get-in tango [color shade])))

(def styles
  {:city  {:thin  {:stroke (BasicStroke. 2.0)
                   :paint  (tango-color :chocolate 1)}
           :thick {:stroke (BasicStroke. 6.0)
                   :paint  (tango-color :chocolate 2)}
           :fill  {:paint  (tango-color :chocolate 0)}}
   :road  {:thin  {:stroke (BasicStroke. 8.0)
                   :paint  (tango-color :aluminium 0)}
           :thick {:stroke (BasicStroke. 12.0)
                   :paint  (tango-color :aluminium 5)}
           :fill  {:paint  (tango-color :aluminium 0)}}
   :field {:thin  {:stroke (BasicStroke. 2.0)
                   :paint  (tango-color :chameleon 1)}
           :thick {:stroke (BasicStroke. 6.0)
                   :paint  (tango-color :chameleon 2)}
           :fill  {:paint  (tango-color :chameleon 0)}}})

(defn- move-to! [^Path2D$Double path [x y]]
  (.moveTo path (* x 180.0) (* y 180.0)))

(defn- close! [^Path2D$Double path]
  (.closePath path))

(def add-segment nil)

(defmulti add-segment!
  {:private true
   :arglists '([path segment point])}
  (fn [path segment point]
    (first segment)))

(defmethod add-segment! :line
  [^Path2D$Double path [_] [x y]]
  (.lineTo path (* x 180.0) (* y 180.0)))

(defmethod add-segment! :quad
  [^Path2D$Double path [_ [cx cy]] [x y]]
  (.quadTo path
           (* cx 180.0)
           (* cy 180.0)
           (* x 180.0)
           (* y 180.0)))

(defmethod add-segment! :cube
  [^Path2D$Double path [_ [c1x c1y] [c2x c2y]] [x y]]
  (.curveTo path
           (* c1x 180.0)
           (* c1y 180.0)
           (* c2x 180.0)
           (* c2y 180.0)
           (* x 180.0)
           (* y 180.0)))

(defn- add-region! [path2d region]
  (move-to! path2d (first region))
  (doseq [[segment point]
          (partition-all 2 (concat (rest region)
                                   [(first region)]))]
    (add-segment! path2d segment point))
  (close! path2d))

(defn region-to-path2d [region]
  (doto (Path2D$Double.)
    (add-region! region)))

(defn- add-path! [path2d path]
  (move-to! path2d (first path))
  (doseq [[segment point] (partition-all 2 (rest path))]
    (add-segment! path2d segment point)))

(defn path-to-path2d [path]
  (doto (Path2D$Double.)
    (add-path! path)))

(defn- use-style! [^Graphics2D g style variant]
  (let [{:keys [stroke paint]} (get-in styles [style variant])]
    (when stroke
      (.setStroke g stroke))
    (.setPaint g paint)))

(defn- draw-city! [^Graphics2D g path2d]
  (use-style! g :city :fill)
  (.fill g path2d)
  (use-style! g :city :thick)
  (.draw g path2d)
  (use-style! g :city :thin)
  (.draw g path2d))

(defn- draw-road! [^Graphics2D g path2d]
  (use-style! g :road :thick)
  (.draw g path2d)
  (use-style! g :road :thin)
  (.draw g path2d))

(defn- draw-field! [^Graphics2D g path2d]
  (use-style! g :field :fill)
  (.fill g path2d))

(declare adorned? adorn-tile adorn-city adorn-road adorn-field)

(defn draw-tile! [g tile]
  {:pre [(adorned? tile)]}
  (let [{:keys [cities roads fields]} tile]
    (doseq [[id field] fields]
      (draw-field! g (::path2d field)))
    (doseq [[id city] cities]
      (draw-city! g (::path2d city)))
    (doseq [[id road] roads]
      (draw-road! g (::path2d road)))))

(add-hook #'tile/adorn-tile
  (fn [f tile]
    (-> (adorn-tile tile)
        (vary-meta assoc ::adorned? true)
        (f))))

(defn- adorned? [tile]
  (-> tile meta ::adorned?))

(defn- adorn-tile [tile]
  (update-map tile
              :cities #(map-vals % adorn-city)
              :roads  #(map-vals % adorn-road)
              :fields #(map-vals % adorn-field)))

(defn- adorn-city [city]
  (assoc city ::path2d (region-to-path2d (:region city))))

(defn- adorn-road [road]
  (assoc road ::path2d (path-to-path2d (:path road))))

(defn- adorn-field [field]
  (assoc field ::path2d (region-to-path2d (:region field))))

(defn create-panel [render-fn width height]
  (doto (proxy [JPanel] []
          (paintComponent [g]
            (proxy-super paintComponent g)
            (render-fn g)))
    (.setPreferredSize (Dimension. width height))))

(defn show-tile [tile]
  (doto (JFrame. "Tile")
    (.add (create-panel #(draw-tile! % tile) 180 180))
    (.pack)
    (.show)))

