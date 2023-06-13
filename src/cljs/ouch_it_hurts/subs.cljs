(ns ouch-it-hurts.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re :refer [reg-sub]]))



(reg-sub :db identity)

(reg-sub :filters #(:filters %))
(reg-sub :sorting #(:sorting %))
(reg-sub :paging #(:paging %))

(reg-sub :error-app (fn [{:keys [error]}] error))
(reg-sub :filter-error (fn [{:keys [filter-error]}] filter-error))

(reg-sub :fetching-params #(select-keys % [:filters :sorting :paging]))

(reg-sub :filters/error (fn [{:keys [filter-error]}] filter-error))

(reg-sub :table/info #(select-keys % [:patients :sorting :paging]))

(reg-sub :table/selected? (fn [db [_ id]]
                        (true? (:selected? (->> (get-in db [:patients :data])
                                                (filter #(= (:id %) id))
                                                (into {}))))))

(reg-sub :table/all-rows-selected? #(every? :selected? (get-in % [:patients :data])))

(reg-sub :filters/all-sex-opts-selected? #(empty? (get-in % [:filters :sex-opts])))

(reg-sub :filters/sex-opts-selected? (fn [db [_ opt]]
                                       (contains? (get-in db [:filters :sex-opts]) opt)))

(reg-sub :modal/info #(:modal %))
