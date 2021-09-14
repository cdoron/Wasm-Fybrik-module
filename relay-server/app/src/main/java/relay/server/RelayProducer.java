package relay.server;


import com.google.common.collect.ImmutableList;
import org.apache.arrow.flight.*;
import org.apache.arrow.flight.perf.impl.PerfOuterClass;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.util.Preconditions;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.VectorLoader;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.VectorUnloader;
import org.apache.arrow.vector.types.Types;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Flight Producer for flight server that serves as relay to another
 *  flight server (e.g. the flight server with ExampleProducer).
 *  Depending on the transform boolean, this server may transform
 *  the first column with a constant column.
 */
public class RelayProducer extends NoOpFlightProducer {
    BufferAllocator allocator;
    private boolean transform = false;
    private final FlightClient client;
    private final Location location;
    private final BigIntVector constVec;
    private int RecordsPerBatch = 1024*1024;

    private BigIntVector getConstIntVector() {
        BigIntVector c = new BigIntVector("a", allocator);
        for (int j = 0; j < this.RecordsPerBatch; j++) {
            c.setSafe(j, 13);
        }
        c.setValueCount(this.RecordsPerBatch);
        return c;
    }

    public RelayProducer(Location location, Location remote_location,
                          BufferAllocator allocator, boolean transform) {
        this.transform = transform;
        this.allocator = allocator;
        this.constVec = getConstIntVector();
        this.location = location;
        this.client = FlightClient.builder()
                .allocator(allocator)
                .location(remote_location)
                .build();
    }

    private VectorSchemaRoot transformVectorSchemaRoot(VectorSchemaRoot v) {
        v = v.removeVector(0);
        return v.addVector(0, this.constVec);
    }

    @Override
    public void getStream(CallContext context, Ticket ticket,
                          ServerStreamListener listener) {
        System.out.println("getStream");

        FlightStream s = client.getStream(ticket);
        VectorSchemaRoot root_in;
        VectorSchemaRoot root_out = null;
        VectorLoader loader = null;
        VectorUnloader unloader = null;

        boolean first = true;
        int i = 0;
        while (s.next()) {
            root_in = s.getRoot();
            i++;
            // System.out.println("relay" + i);
            // System.out.println(root_in.getVector(0));
            if (first) {
                // root_out = VectorSchemaRoot.create(root_in.getSchema(), allocator);
                root_out = root_in;
                loader = new VectorLoader(root_out);
                listener.setUseZeroCopy(false);
                listener.start(root_out);
                first = false;
            }
        //     if (transform) {
        //         root_in = transformVectorSchemaRoot(root_in);
        //     }

            unloader = new VectorUnloader(root_in);
            loader.load(unloader.getRecordBatch());

            listener.putNext();
        }
        listener.completed();
        
    }

    @Override
    public FlightInfo getFlightInfo(FlightProducer.CallContext context,
                                    FlightDescriptor descriptor) {
        // Preconditions.checkArgument(descriptor.isCommand());
        // //Perf exec = Perf.parseFrom(descriptor.getCommand());
        // // System.out.println("getFlightInfo " + descriptor);

        // final Schema pojoSchema = new Schema(ImmutableList.of(
        //         Field.nullable("a", Types.MinorType.BIGINT.getType()),
        //         Field.nullable("b", Types.MinorType.BIGINT.getType()),
        //         Field.nullable("c", Types.MinorType.BIGINT.getType()),
        //         Field.nullable("d", Types.MinorType.BIGINT.getType())
        // ));

        // int streamCount = 1;
        // PerfOuterClass.Token token = PerfOuterClass.Token.newBuilder()
        //         .build();
        // final Ticket ticket = new Ticket(token.toByteArray());

        // List<FlightEndpoint> endpoints = new ArrayList<>();
        // for (int i = 0; i < streamCount; i++) {
        //     endpoints.add(new FlightEndpoint(ticket, location));
        // }
        System.out.println("getFlightInfo1");

        // return new FlightInfo(pojoSchema, descriptor, endpoints, -1, -1);



    

        final CallHeaders callHeaders = new FlightCallHeaders();
        final HeaderCallOption clientProperties = new HeaderCallOption(callHeaders);

        Map<String, Object> request = new HashMap<String, Object>();
        request.put("asset", "nyc-taxi.parquet");
        List<String> list = new ArrayList<String>();
        list.add("vendor_id");
        request.put("columns", list);


        Gson json = new Gson();
        String jsonStr = json.toJson(request);
        System.out.println("getFlightInfo2");
        // FlightDescriptor flightDescriptor = FlightDescriptor.command(jsonStr.getBytes(StandardCharsets.UTF_8));
        FlightDescriptor flightDescriptor = FlightDescriptor.command("123".getBytes(StandardCharsets.UTF_8));
        System.out.println("getFlightInfo3");
        FlightInfo flightInfo = client.getInfo(flightDescriptor, clientProperties);
        System.out.println("getFlightInfo4");
        System.out.println(flightInfo.getSchema());
        // FlightEndpoint flightEndpoint = flightInfo.getEndpoints().get(0);
        // Ticket ticket = flightEndpoint.getTicket();
        // System.out.println(ticket);
        System.out.println("getFlightInfo5");
        return flightInfo;
    }
    
}
