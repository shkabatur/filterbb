(require '[babashka.fs :as fs]
         '[clojure.tools.reader.edn :as edn]
         '[clojure.pprint :refer [pprint]])

(def upstreams (-> "upstreams.edn"
                   slurp
                   edn/read-string))
(def upstream-keys (keys upstreams))

(def path "nginx/conf.d")
(def files (map str (fs/glob path "*.conf")))

(defn replace-by-body [upstream-name]
  (if-let [body (get upstreams upstream-name)]
      body
      upstream-name))

(defn parse-proxy [file-name]
  (let [proxy (->> file-name
                   slurp
		           (re-seq #"proxy_pass\s+http://([^/;]*)")
                   (map second)
                   distinct
                   (map clojure.string/trim)
                   (map replace-by-body)
				   (flatten))]
    {file-name proxy}))

(->> files
     (map parse-proxy)
     (reduce conj)
     pprint)

#_(pprint (flatten (map replace-by-body ["sm_ru" "ostin_mobile"])))

