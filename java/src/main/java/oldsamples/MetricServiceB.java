package oldsamples;

//@Component
//@BifrostService
//public class MetricServiceB implements BifrostEnabled {
//
//    @Autowired
//    private MessagebusService bus;
//
//    ExecutorService executorService;
//
//    private final Logger log = LoggerFactory.getLogger(this.getClass());
//
//    MetricServiceB() {
//        executorService = Executors.newFixedThreadPool(10);
//    }
//
//    @Override
//    public void initialize() {
//        log.info("Initializing Metrics-B Service");
//        Metrics metric = new Metrics(bus, 1500, "metrics-b");
//        executorService.submit(metric);
//    }
//}
//
