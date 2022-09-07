(require '[clojure.pprint :refer [pprint]]
		 '[clojure.string :as s])

#_(def text (slurp "nginx/upstream.conf"))
(def text (slurp "nginx/nginx_config_dump"))
(defn to-map [[name servers]]
 (let [name (s/trim name)
       #_#_servers (s/split-lines servers)
       servers (s/split servers #"\;") ;Вроде бы как решаю проблему несколькоих объявлений на одной строке.
       servers (map s/trim servers)
	   servers (filter #(not-empty %) servers)
	   #_#_servers (filter #(not (s/starts-with? % "#")) servers) ; Избавляемся от закомменченных серверов
	   servers (filter #(s/starts-with? % "server") servers)
	   servers (map #(second (s/split % #"\s+")) servers)
       #_#_servers (map s/trim servers)
	   servers (vec servers)]
  {name servers}))

(->> text
	 (re-seq #".*upstream\s+(.*)\{([^\}]+)\}")
	 (map rest)
	 (map to-map)
	 (reduce conj)
	 (pprint))
