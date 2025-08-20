# AttendanceSync

A robust biometric attendance synchronization system that syncs punch data from local databases to remote web services.

## Features

- ✅ **Configurable**: All settings via properties file
- ✅ **Portable**: Deploy anywhere with Java
- ✅ **Robust**: Error handling and retry logic
- ✅ **Logging**: Comprehensive logging system
- ✅ **Multi-Machine**: Support for multiple biometric machines
- ✅ **Database Agnostic**: Works with SQL Server and can be adapted for other databases

## Quick Start

### 1. Build the Project
```bash
./scripts/build.sh
```

### 2. Configure
Edit `config/application.properties`:
```properties
# Database
db.host=your-db-host
db.username=your-username
db.password=your-password

# School
school.code=your-school-code
school.name=Your School Name

# API
api.primary.url=https://your-api-endpoint.com/attendance
```

### 3. Run
```bash
./scripts/run.sh
```

## Testing

### Test Database Connection
```bash
./scripts/run.sh --test-connection
```

### Test API Connectivity
```bash
./scripts/run.sh --test-api
```

## Deployment

### Deploy to Another Server
```bash
./scripts/deploy.sh
# Follow prompts to specify target directory
```

### Package for Distribution
```bash
tar -czf AttendanceSync-v2.0.tar.gz build/ config/ scripts/ README.md
```

## Configuration Reference

| Property | Description | Default |
|----------|-------------|---------|
| `db.host` | Database server hostname | `localhost` |
| `db.port` | Database server port | `1433` |
| `db.name` | Database name | `Realtime` |
| `db.username` | Database username | `sa` |
| `db.password` | Database password | _(empty)_ |
| `school.code` | School identifier code | `demo` |
| `school.name` | Full school name | `Demo School` |
| `api.primary.url` | Primary API endpoint | _(required)_ |
| `api.timeout` | Request timeout (ms) | `30000` |
| `app.sleep.interval` | Sync interval (ms) | `60000` |
| `machine.ids` | Supported machine IDs | `101,102,103,104,105,106` |

## Troubleshooting

### Database Connection Issues
1. Verify SQL Server is running
2. Check connection string parameters
3. Ensure user has proper permissions
4. Test with: `./scripts/run.sh --test-connection`

### API Connection Issues
1. Verify API endpoint is accessible
2. Check firewall/network settings
3. Validate school code
4. Test with: `./scripts/run.sh --test-api`

## Logs

Application logs are stored in `logs/attendance-sync.log`

## Support

For issues and questions, check the logs first, then review the configuration.