import java.util.*;

class City {
    double x;
    double y;
    String name;

    City(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof City))
            return false;
        City other = (City)o;
        return Double.compare(this.x, other.x) == 0 &&
                Double.compare(this.y, other.y) == 0 &&
                this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return (int) (31 * name.hashCode() + 7*x + 19*y);
    }
}

class Chromosome {
    List<City> citiesInPath;
    double fitness;

    Chromosome(List<City> newCitiesInPath)  {
        citiesInPath = new ArrayList<>();
        citiesInPath.addAll(newCitiesInPath);
    }

    double getFitness(List<City> citiesInPath) {
        int citiesCount = citiesInPath.size();
        double totalDistance = 0;

        for (int i = 0; i < citiesCount - 1; i++) {
            totalDistance += getDistBetweenCities(citiesInPath.get(i),
                    citiesInPath.get(i+1));
        }
        return 1.0/totalDistance;
    }

    double getDistBetweenCities(City first, City second) {
        return Math.sqrt(Math.pow((first.x - second.x), 2) +
                Math.pow((first.y - second.y), 2));
    }
}


public class Main {

    private static final Random RAND = new Random();

    static void evaluate(List<Chromosome> population) {
        for (Chromosome chromosome: population) {
            chromosome.fitness = chromosome.getFitness(chromosome.citiesInPath);
        }
        population.sort(Comparator.comparingDouble(chromosome -> -chromosome.fitness));
    }

    static List<Chromosome> initializePopulation(List<City> cities, int populationSize) {
        List<Chromosome> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            List<City> shuffled = new ArrayList<>(cities);
            Collections.shuffle(shuffled);

            Chromosome chromosome = new Chromosome(shuffled);
            population.add(chromosome);
        }
        return population;
    }

    static Chromosome selectParentTournament(List<Chromosome> population, int k) {
        Chromosome best = null;
        for (int i = 0; i < k; i++) {
            Chromosome candidate = population.get(RAND.nextInt(population.size()));
            if (best == null || candidate.fitness > best.fitness) {
                best = candidate;
            }
        }
        return best;
    }

    static List<Chromosome> selectParents(List<Chromosome> population, int populationSize) {
        List<Chromosome> parents = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            parents.add(selectParentTournament(population, 3));
        }
        return parents;
    }

    static private Chromosome crossOverChromosomes(Chromosome parent1, Chromosome parent2) {   // PMX crossover
        int size = parent1.citiesInPath.size();
        List<City> childGenes = new ArrayList<>(Collections.nCopies(size, (City) null));
        Set<City> used = new HashSet<>();
        int cut1 = RAND.nextInt(size);
        int cut2 = RAND.nextInt(size);
        if (cut1 > cut2) {
            int tmp = cut1;
            cut1 = cut2;
            cut2 = tmp;
        }
        for (int i = cut1; i <= cut2; i++) {
            City c = parent1.citiesInPath.get(i);
            childGenes.set(i, c);
            used.add(c);
        }
         Map<City, City> mapping = new HashMap<>();
        for (int i = cut1; i <= cut2; i++) {
            City g1 = parent1.citiesInPath.get(i);
            City g2 = parent2.citiesInPath.get(i);
            mapping.put(g2, g1);
        }
        for (int i = 0; i < size; i++) {
            if (i >= cut1 && i <= cut2) continue;
            City gene = parent2.citiesInPath.get(i);
             while (used.contains(gene)) {
                 City mapped = mapping.get(gene);
                 if (mapped == null || mapped == gene) {
                      break;
                 }
                 gene = mapped;
             }
             if (gene != null && !used.contains(gene)) {
                  childGenes.set(i, gene);
                  used.add(gene);
             } else {
                  for (City c : parent2.citiesInPath) {
                      if (!used.contains(c)) {
                          childGenes.set(i, c);
                          used.add(c); break;
                      }
                  }
             }
        }
        return new Chromosome(childGenes);
    }

    static List<Chromosome> crossOverPopulation(List<Chromosome> parentsPopulation, int populationSize) {
        List<Chromosome> children = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            Chromosome p1 = parentsPopulation.get(RAND.nextInt(populationSize));
            Chromosome p2 = parentsPopulation.get(RAND.nextInt(populationSize));

            children.add(crossOverChromosomes(p1, p2));
        }
        return children;
    }

    static void mutateChromosome(Chromosome chromosome) {
        int size = chromosome.citiesInPath.size();
        if (size < 2) {
            return;
        }
        int i = RAND.nextInt(size);
        int j = RAND.nextInt(size);

        while (j == i) {
            j = RAND.nextInt(size);
        }

        Collections.swap(chromosome.citiesInPath, i, j);
    }

    static void mutate(List<Chromosome> population, double mutationRate) {
        for (Chromosome chromosome : population) {
            if (RAND.nextDouble() < mutationRate) {
                mutateChromosome(chromosome);
            }
        }
    }

    static List<Chromosome> selectivePurge(List<Chromosome> parents,
                                           List<Chromosome> children,
                                           int populationSize) {
        parents.sort(Comparator.comparingDouble(ch -> -ch.fitness));
        children.sort(Comparator.comparingDouble(ch -> -ch.fitness));

        List<Chromosome> result = new ArrayList<>(populationSize);

        int p = 0;
        int c = 0;
        while (result.size() < populationSize && (p < parents.size() || c < children.size())) {

            if (p >= parents.size()) {
                result.add(children.get(c++));
            } else if (c >= children.size()) {
                result.add(parents.get(p++));
            } else {
                if (parents.get(p).fitness >= children.get(c).fitness) {
                    result.add(parents.get(p++));
                } else {
                    result.add(children.get(c++));
                }
            }
        }
        return result;
    }

    static void addRandomImmigrants(List<Chromosome> population,
                                    List<City> cities,
                                    int count) {
        for (int i = 0; i < count; i++) {
            List<City> shuffled = new ArrayList<>(cities);
            Collections.shuffle(shuffled);
            population.add(new Chromosome(shuffled));
        }
    }

    static void geneticAlgorithmSolve(List<City> cities, int populationSize, int generations, boolean randomPoints) {
        List<Chromosome> population = initializePopulation(cities, populationSize);
        evaluate(population);
        System.out.println(1.0/population.getFirst().fitness);

        //generations = 200;
        int numPrints = 10;
        int step = generations / (numPrints - 1);

        Set<Integer> printGens = new LinkedHashSet<>();
        printGens.add(0);

        for (int i = 1; i < numPrints - 1; i++) {
            printGens.add(i * step);
        }
        printGens.add(generations);

        for (int t = 0; t <= generations; t++) {
            if (printGens.contains(t)) {
                System.out.println(1.0 / population.getFirst().fitness);
            }

            if (t == generations) {
                break;
            }

            List<Chromosome> parents = selectParents(population, populationSize);
            List<Chromosome> children =  crossOverPopulation(parents, populationSize);
            mutate(children, 0.05);
            evaluate(children);

            population = selectivePurge(parents, children, populationSize*9/10);
            addRandomImmigrants(population, cities, populationSize/10);
        }

        System.out.println(' ');
        if (!randomPoints) {
            printPath(population.getFirst());
        }
        System.out.println(1.0/population.getFirst().fitness);
    }

    static void printPath(Chromosome chromosome) {
        int size = chromosome.citiesInPath.size();
        for (int i = 0; i < size; i++) {
            if (i!= size-1) {
                System.out.print(chromosome.citiesInPath.get(i).name + "->");
            } else {
                System.out.println(chromosome.citiesInPath.get(i).name);
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useLocale(Locale.US);

        List<City> citiesInPath = new ArrayList<>();

        if (sc.hasNextInt()) {
            int N = sc.nextInt();
            Random rand = new Random();

            for (int i = 0; i < N; i++) {
                double x = rand.nextDouble() * 1000.0;
                double y = rand.nextDouble() * 1000.0;
                String cityName = "City" + i;
                citiesInPath.add(new City(x, y, cityName));
            }
            int generations = 50 + N;
            int populationSize = 50 + N * 10;
            geneticAlgorithmSolve(citiesInPath, populationSize, generations, true);
        } else {
            String dataSetName = sc.next();
            int N = sc.nextInt();

            for (int i = 0; i < N; i++) {
                String cityName = sc.next();
                double x = sc.nextDouble();
                double y = sc.nextDouble();
                citiesInPath.add(new City(x, y, cityName));            }

            int generations = 50 + N;
            int populationSize = 50 + N * 1000;
            geneticAlgorithmSolve(citiesInPath, populationSize, generations, false);
        }
    }
}