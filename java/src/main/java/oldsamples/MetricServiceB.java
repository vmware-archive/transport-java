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
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    MetricServiceB() {
//        executorService = Executors.newFixedThreadPool(10);
//    }
//
//    @Override
//    public void initializeSubscriptions() {
//        logger.info("Initializing Metrics-B Service");
//        Metrics metric = new Metrics(bus, 1500, "metrics-b");
//        executorService.submit(metric);
//    }
//}
//
