(ns thiudinassus.tile-test
  (:use clojure.test
        thiudinassus.tile))

(deftest test-rotate-cdir
  (are [steps res-dir]
       (= (rotate-cdir steps :n) res-dir)
        0 :n,   1 :e,   2 :s,   3 :w,   4 :n
               -1 :w,  -2 :s,  -3 :e,   4 :n)
  (are [in-dir res-dir]
       (= (rotate-cdir +1 in-dir) res-dir)
       :n :e,   :e :s,   :s :w,   :w :n)
  (are [in-dir res-dir]
       (= (rotate-cdir -1 in-dir) res-dir)
       :n :w,   :e :n,   :s :e,   :w :s))

(deftest test-rotate-idir
  (are [steps res-dir]
       (= (rotate-idir steps [:n :e]) res-dir)
       0 [:n :e],   1 [:e :s],   2 [:s :w],   3 [:w :n],   4 [:n :e]
                   -1 [:w :n],  -2 [:s :w],  -3 [:e :s],  -4 [:n :e]))

(deftest test-edge-type-matches?
  (testing "two of the edge type match"
    (are [x]
         (edge-type-matches? x x)
         :city :road :field))
  (testing "all edge types match nil"
    (are [x]
         (edge-type-matches? x nil)
         :city :road :field))
  (testing "nil matches all edge types"
    (are [x]
         (edge-type-matches? nil x)
         :city :road :field))
  (testing "one edge type does not match another one"
    (are [x y]
         (not (edge-type-matches? x y))
         :city :road,    :city :field
         :road :city,    :road :field
         :field :city,   :field :road)))

(deftest test-signature-matches?
  (testing "a signature matches itself"
    (is (signature-matches? [:road :field :road :city]
                            [:road :field :road :city])))
  (testing "a signature matches itself with some nils"
    (is (signature-matches? [:road :field :road :city]
                            [nil   :field :road nil])))
  (testing "a signarure matches all nils"
    (is (signature-matches? [:road :field :road :city]
                            [nil nil nil nil])))
  (testing "a signature does not necessarily match itself rotated"
    (is (not (signature-matches? [:road :field :road :city]
                                 [:city :road :field :road]))))
  (testing "a signature does not match a completely different one"
    (is (not (signature-matches? [:road :field :road :city]
                                 [:city :road :road :city])))))

(deftest test-rotate-signature
  (are [steps res]
       (rotate-signature steps [:road :field :road :city])
       -4 [:road :field :road :city]
       -3 [:city :road :field :road]
       -2 [:road :city :road :field]
       -1 [:field :road :city :road]
        0 [:road :field :road :city]
        1 [:city :road :field :road]
        2 [:road :city :road :field]
        3 [:field :road :city :road]
        4 [:road :field :road :city]))

