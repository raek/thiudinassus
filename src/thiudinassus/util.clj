(ns thiudinassus.util)

(defn assoc-once
  "Like assoc, but throws an IllegalStateException if the key is already present."
  ([m key val]
     (if (contains? m key)
       (throw (IllegalStateException.
               (str "Key already present: " key)))
       (assoc m key val)))
  ([m key val & kvs]
     (let [ret (assoc-once m key val)]
       (if kvs
         (recur ret (first kvs) (second kvs) (nnext kvs))
         ret))))

(defn map-coll
  "Apply f to each element in `coll` and collects the results in a
   collection of the same type. Note: this function takes the
   collection as the first argument."
  [coll f]
  (into (empty coll) (map f coll)))

(defn map-keys
  "Apply f to each key in the map and return a new map with the new
   keys. Note: this function takes the collection as the first
   argument."
  [m f]
  (into (empty m)
        (for [[k v] m]
          [(f k) v])))

(defn map-vals
  "Apply f to each value in the map and return a new map with the new
   values. Note: this function takes the collection as the first
   argument."
  [m f]
  (into (empty m)
        (for [[k v] m]
          [k (f v)])))

(declare update-map*)

(defn update-map
  "Take a source map and key-function pairs and make a new map where
   all the listed keys are associated with the accompanied function
   applied to the value in the source map for that key.

   Examples:

       (update-map {:a 1, :b 2}, :a dec, :b inc)
         => {:a 0, :b 3}"
  [m & key-fn-pairs]
  (update-map* m (apply hash-map key-fn-pairs)))

(defn update-map*
  "Take a source map and a map from key to functions and make a new
   version of the source map where all the keys present in the
   function map are associated with the corresponding function in the
   function map applied to the corresponding value in the source map.

   Examples:

       (update-map* {:a 1, :b 2} {:a dec, :b inc)
         => {:a 0, :b 3}"
   [m fn-map]
  (let [f (fn [acc [k f]]
            (assoc acc k (f (get acc k))))]
    (reduce f m fn-map)))
