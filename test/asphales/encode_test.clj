(ns asphales.encode-test
  (:require [clojure.test :refer :all]
            [asphales.encode :refer :all]
            [asphales.token :as token]))

(deftest scalar-encoding
  (testing "Encode nil"
    (is (= "nil" (encode nil))))

  (testing "Encode integer values"
    (is (= "0" (encode 0)))
    (is (= "-1" (encode -1)))
    (is (= "1" (encode 1)))
    (is (= "-10" (encode -10)))
    (is (= "10" (encode 10))))

  (testing "Encode BigInt values"
    (is (= "0N" (encode 0N)))
    (is (= "-1N" (encode -1N)))
    (is (= "1N" (encode 1N)))
    (is (= "-10N" (encode -10N)))
    (is (= "10N" (encode 10N))))

  (testing "Encode BigDecimal values"
    (is (= "0M" (encode 0M)))
    (is (= "-1M" (encode -1M)))
    (is (= "1M" (encode 1M)))
    (is (= "-10M" (encode -10M)))
    (is (= "10M" (encode 10M)))
    (is (= "3.14M" (encode 3.14M)))
    (is (= "-3.14M" (encode -3.14M))))

  (testing "Encode boolean values"
    (is (= "true" (encode true)))
    (is (= "false" (encode false))))

  (testing "Encode character values"
    (is (= "\\h" (encode \h)))
    (is (= "\\newline" (encode \newline))))

  (testing "Encode string values"
    (is (= "\"\"" (encode "")))
    (is (= "\"a\"" (encode "a")))
    (is (= "\"hello world\"" (encode "hello world")))
    (is (= "\"   hello world\"" (encode "   hello world")))
    (is (= "\"\\thello world\"" (encode "\thello world")))
    (is (= "\"hello world   \"" (encode "hello world   ")))
    (is (= "\"hello world\\t\"" (encode "hello world\t")))
    (is (= "\"\\n\"" (encode "\n")))
    (is (= "\"\\r\"" (encode "\r")))
    (is (= "\"\\r\\n\"" (encode "\r\n")))
    (is (= "\"\\n\\r\"" (encode "\n\r"))))

  (testing "Encode keyword values"
    (is (= ":x" (encode :x)))
    ;; Keywords with explicit namespaces are not allowed
    (is (thrown? RuntimeException (encode ::x))))

  (testing "Encode date values"
    (is (= "#inst \"2022-02-14T11:20:00.123-00:00\""
           (encode #inst "2022-02-14T11:20:00.123-00:00")))))

(deftest sequence-encoding
  (testing "Encode Vectors"
    (is (= "[]" (encode [])))
    (is (= "[1]" (encode [1])))
    (is (= "[1 2 3]" (encode [1 2 3])))
    (is (= "[[1] [2] [3]]" (encode [[1] [2] [3]]))))

  (testing "Encode Lists"
    (is (= "()" (encode ())))
    (is (= "(1)" (encode '(1))))
    (is (= "(1 2 3)" (encode '(1 2 3))))
    (is (= "((1) (2) (3))" (encode '((1) (2) (3)))))))

(deftest unordered-encoding
  (testing "Encode Sets"
    (is (= "#{}" (encode #{})))
    (is (= "#{1}" (encode #{1})))
    (is (= "#{1 2 3}" (encode #{1 2 3})))
    (is (= "#{1 2 3}" (encode #{3 2 1})))
    (is (= "#{1 2 3 10}" (encode #{1 2 3 10})))
    (is (= "#{1 2 3 10}" (encode #{3 2 1 10})))
    (is (= "#{\"a\" \"b\" \"c\"}" (encode #{"c" "b" "a"})))

    ;; Elements to be ordered must be comparable with each other
    (is (thrown? RuntimeException (encode #{"a" 1})))

    ;; Elements to be ordered must be comparable at all
    (is (thrown? RuntimeException (encode #{'(1) '(2)}))))

  (testing "Encode sets of tokens"
    (is (= "#{#token \"token-1\"}"
           (encode #{(token/token "token-1")})))
    (is (= "#{#token \"token-1\" #token \"token-2\" #token \"token-3\"}"
           (encode #{(token/token "token-3")
                     (token/token "token-2")
                     (token/token "token-1")}))))

  (testing "Encode Maps"
    (is (= "{}" (encode {})))
    (is (= "{:x 3 :y 4}" (encode {:x 3 :y 4})))
    (is (= "{:x 3 :y 4}" (encode {:y 4 :x 3})))
    (is (= "{:length 5 :x 3 :y 4}" (encode {:y 4 :x 3 :length 5}))))

  (testing "Encode Nested Maps and Sets"
    (is (= "{:desc \"Point\" :point {:x 3 :y 4}}"
           (encode {:point {:x 3 :y 4} :desc "Point"})))
    (is (= "{:observers #{:alice :bob} :x 3 :y 4}"
           (encode {:y 4 :x 3 :observers #{:alice :bob}})))))

