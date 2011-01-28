(ns thiudinassus.scratch
  (:require (thiudinassus [tile :as tile]
                          [graphics :as gfx])))

(def tile
  (-> {:edges {:n [:road :f0 :r0 :f1]
               :e [:road :f1 :r0 :f0]
               :s [:city :c0]
               :w [:city :c0]}
       :cities {:c0 {:edges #{:s :w}
                     :marker [0.125 0.5]
                     :region [[0.0 0.0]
                              [:quad [0.25 0.25]]
                              [0.25 0.5]
                              [:quad [0.25 0.75]]
                              [0.5 0.75]
                              [:quad [0.75 0.75]]
                              [1.0 1.0]
                              [:line]
                              [0.0 1.0]
                              [:line]]}}
       :roads  {:r0 {:edges #{:n :e}
                     :path [[0.5 0.0]
                            [:quad [0.5 0.5]]
                            [1.0 0.5]]}}
       :fields {:f0 {:edges #{[:n :w] [:e :s]}
                     :region [[0.0 0.0]
                              [:line]
                              [0.5 0.0]
                              [:quad [0.5 0.5]]
                              [1.0 0.5]
                              [:line]
                              [1.0 1.0]
                              [:quad [0.75 0.75]]
                              [0.5 0.75]
                              [:quad [0.25 0.75]]
                              [0.25 0.5]
                              [:quad [0.25 0.25]]]}
                :f1 {:edges #{[:n :e] [:e :n]}
                     :region [[0.5 0.0]
                              [:line]
                              [1.0 0.0]
                              [:line]
                              [1.0 0.5]
                              [:quad [0.5 0.5]]]}}}
      tile/adorn-tile))

