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
Il progetto ha come obiettivo l’analisi delle dipendenze tra classi, interfacce e package in un progetto Java, ovvero sull’identificazione di quali elementi del codice utilizzano altri.

L’analisi delle dipendenze è un’operazione potenzialmente costosa in termini computazionali, soprattutto su grandi repository di codice.

La difficoltà non sta solo nell’analizzare i singoli file, ma nel mettere insieme i risultati in modo coerente, tenendo conto dell’intero progetto. 
Per affrontare questo problema, il progetto richiede di sviluppare due soluzioni distinte, basate su modelli di esecuzione diversi:

- Un **approccio asincrono**, in cui l’analisi viene eseguita in modo non bloccante e il risultato finale è disponibile solo una volta completato il processo.

- Un **approccio reattivo**, in cui i risultati vengono forniti progressivamente, man mano che l’analisi procede, permettendo all’utente di visualizzare l’avanzamento in tempo reale.

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