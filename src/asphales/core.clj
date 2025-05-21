(ns asphales.core)

(defprotocol Encodable
  "Protocol for encoding values in normalized form for storage."
  (encode-value [value]))

(defn- encode-simple [value]
  (pr value))

(defn- encode-sequence [values begin end]
  (print begin)
  (reduce (fn [needs-space? v]
            (when needs-space?
              (print " "))
            (encode-value v)
            true)
          false values)
  (print end))

(defn- encode-map [value]
  (print "{")
  (reduce (fn [needs-space? k]
            (when needs-space?
              (print " "))
            (encode-value k)
            (print " ")
            (encode-value (get value k))
            true)
          false (sort (keys value)))
  (print "}"))

(extend-protocol Encodable
  nil
  (encode-value [value]
    (encode-simple value))

  java.lang.Boolean
  (encode-value [value]
    (encode-simple value))

  java.lang.Character
  (encode-value [value]
    (encode-simple value))

  java.lang.String
  (encode-value [value]
    (encode-simple value))

  clojure.lang.Keyword
  (encode-value [value]
    (encode-simple value))

  clojure.lang.BigInt
  (encode-value [value]
    (encode-simple value))

  java.math.BigDecimal
  (encode-value [value]
    (encode-simple value))

  java.lang.Long
  (encode-value [value]
    (encode-simple value))

  clojure.lang.PersistentVector
  (encode-value [value]
    (encode-sequence value "[" "]"))

  clojure.lang.PersistentList$EmptyList
  (encode-value [value]
    (encode-sequence value "(" ")"))

  clojure.lang.PersistentList
  (encode-value [value]
    (encode-sequence value "(" ")"))

  clojure.lang.PersistentHashSet
  (encode-value [value]
    (encode-sequence (sort value) "#{" "}"))

  clojure.lang.PersistentArrayMap
  (encode-value [value]
    (encode-map value)))

(defn encode [value]
  (with-out-str
    (encode-value value)))

(defn -main
  "I don't do a whole lot."
  []
  (doseq [x [nil 1 "hello" [] [1] [1 2 3] () '(1 2 3)]]
    (println (str x " -> " (encode x)))))
