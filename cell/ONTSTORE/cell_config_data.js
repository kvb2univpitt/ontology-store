{
    files: [
        "i2b2_msgs.js"
    ],
    config: {
        name: "OntStore Cell",
        description: "OntologyStore Cell",
        category: ["core", "cell"],
        paramTranslation: [
            {xmlName: 'ShpMax', thinClientName: 'max', defaultValue: 500},
            {xmlName: 'ShpHiddens', thinClientName: 'hiddens', defaultValue: false},
            {xmlName: 'ShpSynonyms', thinClientName: 'synonyms', defaultValue: true}
        ]
    }
}