(ns asphales.token
  (:require [asphales.encode :as encode]))

(deftype Token [digest])

(defn token [digest]
  (Token. digest))

(defn token-digest [token]
  (.digest token))

(extend-protocol encode/Encodable
  Token
  (encode-value [value]
    (print "#token ")
    (pr (token-digest value))))
