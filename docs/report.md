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
Il progetto ha l’obiettivo di sviluppare una libreria capace di analizzare le dipendenze di un’applicazione Java a tre livelli: l’intero progetto, una specifica classe o un singolo package, al fine di identificare da quali elementi del codice dipende la porzione analizzata.

In particolare, lo sviluppo prevede due soluzioni distinte, basate su approcci differenti:

- **Asincrono**: l’esecuzione viene suddivisa in task, ciascuno dei quali è eseguito in modo indipendente, senza bloccare il flusso principale del programma. Il risultato di ogni operazione è reso disponibile al termine della sua esecuzione.
  
- **Reattivo**: paradigma in cui il programma è modellato come una reazione a eventi esterni, orientato al flusso di dati e alla propagazione dei cambiamenti. I componenti reagiscono automaticamente a eventi o modifiche di stato, utilizzando stream asincroni e osservabili. In questo modello, i risultati si propagano come un flusso di valori nel tempo, anziché in un unico momento, permettendo all’utente finale di visualizzare l’avanzamento in tempo reale.

Per la realizzazione della parte reattiva, è necessario sviluppare un’interfaccia grafica che consenta all’utente di avviare l’analisi e visualizzare dinamicamente, incrementalmente le dipendenze trovate nel progetto fornito.
Le relazioni devono essere mostrate sotto forma di grafo, possibilmente raggruppando le classi nei rispettivi package.
L’interfaccia deve includere due pulsanti (uno per selezionare la root folder del progetto e uno per avviare l’analisi), una sezione per la visualizzazione del grafo e due contatori (uno per il numero di classi analizzate e uno per il numero di dipendenze individuate).

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
È la parte di programmazione reattiva, gestisce flussi di eventi multipli (input utente, aggiornamento grafico, ricezione dei risultati) tramite _ReactiveX_ (_RxJava_).

La GUI è composta di tre componenti:

- Selettore della root del progetto.

- Pulsante di avvio analisi.

- Pannello visualizzazione dipendenze (grafo interattivo).

L'interfaccia grafica permette di visualuzzare dinamicamente il progresso: le classi vengono analizzate e le dipendenze trovate sono progressivamente aggiunte nel grafo.

Viene seguito un pattern MVC per garantire separazione tra interfaccia grafica e logica applicativa, attraverso l'utilizzo di un _Controller_ che collega le due parti.

## Comportamento del sistema
### DependencyAnalyzerLib
### DependencyAnalyzer
