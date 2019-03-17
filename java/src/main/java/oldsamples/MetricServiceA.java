package oldsamples;

//@Component
////@BifrostService
//public class MetricServiceA implements BifrostEnabled {
//
//    @Autowired
//    private MessagebusService bus;
//
//    ExecutorService executorService;
//
//    private final Logger log = LoggerFactory.getLogger(this.getClass());
//
//    MetricServiceA() {
//        executorService = Executors.newFixedThreadPool(10);
//    }
//
//    @Override
//    public void initialize() {
//        log.info("Initializing Metrics-A Service");
//        Metrics metric = new Metrics(bus, 100, "metrics-a");
//        executorService.submit(metric);
//    }
//}
//
