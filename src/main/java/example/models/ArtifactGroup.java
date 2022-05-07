/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */

package example.models;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.graphql.subscriptions.annotations.Subscription;
import com.yahoo.elide.graphql.subscriptions.annotations.SubscriptionField;

import lombok.Data;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.ngram.NGramTokenizerFactory;
import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Include(name = "group")
@Indexed
@Table(name = "artifactgroup")
@Entity
@Subscription
@Data
@AnalyzerDef(name = "case_insensitive",
        tokenizer = @TokenizerDef(factory = NGramTokenizerFactory.class, params = {
                @Parameter(name = "minGramSize", value = "3"),
                @Parameter(name = "maxGramSize", value = "50")
        }),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class)
        }
)
public class ArtifactGroup {
    @Id
    private String name = "";

    @SubscriptionField
    @Fields({
            @Field(name = "commonName", index = Index.YES,
                    analyze = Analyze.YES, store = Store.NO, analyzer = @Analyzer(definition = "case_insensitive")),
    })
    private String commonName = "";

    @SubscriptionField
    private String description = "";

    @SubscriptionField
    @OneToMany(mappedBy = "group")
    private List<ArtifactProduct> products = new ArrayList<>();
}
