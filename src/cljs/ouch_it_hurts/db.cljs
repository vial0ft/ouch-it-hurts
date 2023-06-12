(ns ouch-it-hurts.db)

(def default-state {:filters {}
                    :sorting {:id :asc}
                    :paging {:page-number 1
                             :page-size 10}
                    :patients {}
                    :modal nil
                    :error nil
                    :filter-error nil})
