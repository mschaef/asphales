(ns asphales.token-test
  (:require [clojure.test :refer :all]
            [asphales.token :refer :all]))

(deftest token-representation
  (testing "Token digest"
    (is (= "foo" (token-digest (token "foo")))))

  (testing "Token equality"
    (is (= (token "foo") (token "foo")))
    (is (not (= (token "bar") (token "foo")))))

  (testing "Token comparison"
    (let [foo (token "foo")]
      (is (= 0 (compare foo foo)))
      (is (= 0 (compare (token "foo") (token "foo"))))
      (is (> 0 (compare (token "bar") (token "foo"))))
      (is (< 0 (compare (token "foo") (token "bar"))))

      (is (thrown? RuntimeException (compare (token "foo") "foo")))
      (is (thrown? RuntimeException (compare "foo" (token "foo")))))))

