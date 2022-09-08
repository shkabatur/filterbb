(def nginx-config (slurp (first *command-line-args*)))

(defn to-map [[name servers]]
 (let [name (str/trim name)
   servers (str/split servers #"[\n\;]") ;Вроде бы как решаю проблему несколькоих объявлений на одной строке.
   servers (map str/trim servers)
   servers (filter #(not-empty %) servers)
   servers (filter #(str/starts-with? % "server") servers) ;Пропускаем другие объявления, и избавляемся от строк с комментариями
   servers (map #(second (str/split % #"\s+")) servers)
   servers (vec servers)]
   {name servers}))

(def upstreams 
 (->> nginx-config
      (re-seq #".*upstream\s+(.*)\{([^\}]+)\}") ;; Вытаскиваем имя и тело апстрима
      (map rest)
      (map to-map)
      (reduce conj)))

(def upstream-keys (keys upstreams))

(def raw-config-files (->> (str/split nginx-config #"\#\s+configuration\s+file")
                           (filter not-empty)))

(defn parse-config-file [config-file]
  (let [ [[_ name body]] (re-seq #"(?is)(.*[a-zA-Z.\-_]+):\n(.*)" config-file)]
    [(str/trim (or name  "kukuha.edet")) (or body "priehala")]))

(def parsed-config-files (map parse-config-file raw-config-files))

(defn replace-by-body [upstream-name]
  (if-let [body (get upstreams upstream-name)]
      body
      upstream-name))

(defn parse-proxy [[file-name body]]
  (let [proxy (->> body
                   (re-seq #"proxy_pass\s+http://([^/;\$]*)")
                   (map second)
                   distinct
                   (map str/trim)
                   (map replace-by-body)
                   (flatten))]
    (if (not-empty proxy)
        {file-name proxy}
        {})))

(->> parsed-config-files
     (map parse-proxy)
     (filter not-empty)
     (reduce conj)
     pprint)

