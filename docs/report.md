<div align="center">

# Assignment 2

Giangiulli Chiara (1189567; chiara.giangiulli@studio.unibo.it)  
Shtini Dilaver (1189997; dilaver.shtini@studio.unibo.it)  
Terenzi Mirco (1193420; mirco.terenzi@studio.unibo.it)  
12 Maggio 2025

</div>

## Indice

- [Analisi del problema](#analisi-del-problema)
- [Design e Architettura](#design-e-architettura)
- [Comportamento del sistema](#comportamento-del-sistema)

## Analisi del problema
Il progetto ha come obiettivo l'analisi delle dipendenze tra classi, interfacce e package in un’applicazione Java, ovvero identificare da quali elementi del codice dipendono le porzioni analizzate.

L’analisi delle dipendenze è un’operazione potenzialmente costosa in termini computazionali, soprattutto su repository di grandi dimensioni.
La difficoltà non riguarda solo l’analisi dei singoli file, ma anche la combinazione dei risultati in modo coerente.


Per affrontare queste problematiche, lo sviluppo prevede due soluzioni distinte, ciascuna basata su un approccio differente:

- **Asincrono**: è richiesta la realizzazione di una libreria in grado di operare a tre diverse profondità: sull’intero progetto, su un singolo package o su una specifica classe, con l’obiettivo di identificare le relative dipendenze. Le operazioni su ciascuno di questi livelli devono essere eseguite in modo indipendente e non bloccante. 
  
- **Reattivo**: è previsto lo sviluppo di un'interfaccia grafica che permetta all’utente di avviare l’analisi delle dipendenze e visualizzare in modo dinamico e incrementale i risultati ottenuti per il progetto selezionato. Le relazioni devono essere mostrate sotto forma di grafo, eventualmente raggruppando le classi nei rispettivi package. I componenti devono reagire automaticamente a eventi esterni o modifiche di stato. In questo modello, i risultati si propagano come un flusso di valori nel tempo, anziché in un unico momento, permettendo all’utente finale di visualizzare l’avanzamento in tempo reale. L’interfaccia deve includere due pulsanti (uno per selezionare la _root folder_ del progetto e uno per avviare l’analisi), una sezione per la visualizzazione del grafo e due contatori (uno per il numero di classi analizzate e uno per il numero di dipendenze individuate).

## Design e Architettura
Il sistema si articola in due componenti principali:

### DependencyAnalyzerLib
Utilizza un event loop asincrono per la gestione delle richieste, implementato con _Vert.x_, consente l’esecuzione non bloccante delle analisi.
Offre tre metodi principali:

- _getClassDependencies(File classSrcFile) → ClassDepsReport_

- _getPackageDependencies(File packageSrcFolder) → PackageDepsReport_

- _getProjectDependencies(File projectSrcFolder) → ProjectDepsReport_

Usa _JavaParser_ per analizzare i file sorgente e costruire l’AST (Abstract Syntax Tree).

L'output prodotto è strutturato in oggetti _DepsReport_ che rappresentano le dipendenze tra elementi.

### DependencyAnalyzer
È la parte che si occupa della programmazione reattiva, gestisce flussi di eventi multipli (input utente, aggiornamento grafico, ricezione dei risultati) tramite _ReactiveX_ (_RxJava_).

La GUI è composta di tre componenti:

- Selettore della root del progetto.

- Pulsante di avvio analisi.

- Pannello visualizzazione dipendenze (grafo interattivo).

L'interfaccia grafica permette di visualizzare dinamicamente l'analisi: le classi vengono analizzate e le dipendenze trovate sono progressivamente aggiunte nel grafo.

Viene seguito un pattern MVC per garantire separazione tra interfaccia grafica e logica applicativa, attraverso l'utilizzo di un _Controller_ che collega le due parti.

## Comportamento del sistema
### DependencyAnalyzerLib
### DependencyAnalyzer
