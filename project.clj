;; Copyright (c) 2015-2024 Michael Schaeffer
;;
;; Licensed as below.
;;
;; Portions Copyright (c) 2014 KSM Technology Partners
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;       http://www.apache.org/licenses/LICENSE-2.0
;;
;; The license is also includes at the root of the project in the file
;; LICENSE.
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
;;
;; You must not remove this notice, or any other, from this software.

(defproject asphales "0.1.0-SNAPSHOT"
  :description "A simple content addressible storage written in Clojure"
  :url "https://github.com/mschaef/asphales"
  :scm {:name "git"
        :url "https://github.com/mschaef/asphales.git"}
  :license {:name "The Apache Software License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.12.0"]]
  :repl-options {:init-ns asphales.core}

  :plugins [[dev.weavejester/lein-cljfmt "0.13.0"]]

  :deploy-repositories [["releases" {:url "https://repo.clojars.org"}]])
