package Server;

public class Subscription {
	private Connection connection;
	private ResourceServer resourceTemplate;
	private String id;
	Subscription(Connection c, ResourceServer resourceTemplate, String id)
	{
		this.connection = c;
		this.resourceTemplate = resourceTemplate;
		this.id = id;
	}
	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}
	/**
	 * @param connection the connection to set
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	/**
	 * @return the resourceTemplate
	 */
	public ResourceServer getResourceTemplate() {
		return resourceTemplate;
	}
	/**
	 * @param resourceTemplate the resourceTemplate to set
	 */
	public void setResourceTemplate(ResourceServer resourceTemplate) {
		this.resourceTemplate = resourceTemplate;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	
}
