(ns asphales.encode
  (:require [clojure.edn :as edn]))


(deftype Token [digest])

(defn token [digest]
  (Token. digest))

(defn token-digest [token]
  (.digest token))

(defprotocol Encodable
  "Protocol for encoding values in normalized form for storage."
  (encode-value [value]))

(defn- bad-encoding
  ([message value cause]
   (throw (RuntimeException. (str "Error encoding value. " message
                                  ": " value "")
                             cause)))

  ([message value]
   (bad-encoding message value nil)))

(defn- normalize-key-order [keys]
  (try
    (sort keys)
    (catch Exception ex
      (bad-encoding "Cannot normalize key order for storage" keys ex))))

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
          false (normalize-key-order (keys value)))
  (print "}"))

(extend-protocol Encodable
  nil
  (encode-value [value]
    (pr value))

  java.math.BigDecimal
  (encode-value [value]
    (pr value))

  clojure.lang.BigInt
  (encode-value [value]
    (pr value))

  java.lang.Boolean
  (encode-value [value]
    (pr value))

  java.lang.Character
  (encode-value [value]
    (pr value))

  java.util.Date
  (encode-value [value]
    (pr value))

  clojure.lang.Keyword
  (encode-value [value]
    (when (.getNamespace value)
      (bad-encoding "Keywords with namespaces not allowed" value))
    (pr value))

  java.lang.Long
  (encode-value [value]
    (pr value))

  java.lang.String
  (encode-value [value]
    (pr value))

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
    (encode-sequence (normalize-key-order value) "#{" "}"))

  clojure.lang.PersistentArrayMap
  (encode-value [value]
    (encode-map value))

  Token
  (encode-value [value]
    (print "#token ")
    (pr (token-digest value))))

(defn encode [value]
  (with-out-str
    (encode-value value)))

(defn encode-binary [value]
  (.getBytes (encode value) "UTF-8"))

(defn decode-binary [bytes]
  (edn/read-string {:readers {'token token}}
                   (String. bytes "UTF-8")))

