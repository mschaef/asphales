(ns asphales.core-test
  (:require [clojure.test :refer :all]
            [asphales.core :refer :all]))

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
    (is (= "\"\\n\\r\"" (encode "\n\r")))))

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
